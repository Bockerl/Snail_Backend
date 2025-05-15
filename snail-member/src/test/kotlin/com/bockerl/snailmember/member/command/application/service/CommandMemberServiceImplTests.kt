@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.file.command.application.service.CommandFileService
import com.bockerl.snailmember.infrastructure.outbox.dto.OutboxDTO
import com.bockerl.snailmember.infrastructure.outbox.enums.EventType
import com.bockerl.snailmember.infrastructure.outbox.service.OutboxService
import com.bockerl.snailmember.member.command.domain.aggregate.entity.Member
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.MemberStatus
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberDeleteEvent
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberLoginEvent
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberUpdateEvent
import com.bockerl.snailmember.member.command.domain.repository.MemberRepository
import com.bockerl.snailmember.member.command.domain.service.CommandMemberServiceImpl
import com.bockerl.snailmember.utils.*
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.date.after
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.context.ApplicationEventPublisher
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.time.LocalDateTime

class CommandMemberServiceImplTests :
    BehaviorSpec({
        // mock
        val memberRepository = mockk<MemberRepository>()
        val commandFileService = mockk<CommandFileService>(relaxed = true)
        val eventPublisher = mockk<ApplicationEventPublisher>()
        val outBoxService = mockk<OutboxService>()
        val objectMapper =
            ObjectMapper().apply {
                registerModule(KotlinModule.Builder().build())
                registerModule(JavaTimeModule())
                enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        // test 구현체
        lateinit var memberService: CommandMemberService

        beforeContainer {
            clearMocks(
                memberRepository,
                eventPublisher,
                commandFileService,
                outBoxService,
                answers = false,
            )

            memberService =
                CommandMemberServiceImpl(
                    memberRepository,
                    commandFileService,
                    outBoxService,
                    eventPublisher,
                    objectMapper,
                )
        }

        // 마지막 접속 시간 업데이트 테스트
        Given("사용자가 로그인해서") {
            val email = TEST_EMAIL
            val wrongEmail = "wrong_email"
            val beforeTime = LocalDateTime.now().minusDays(1)
            val member = createMember()
            val memberSlot = slot<Member>()
            val eventSlot = slot<Any>()
            every {
                memberRepository.findMemberByMemberEmailAndMemberStatusNot(
                    email,
                    MemberStatus.ROLE_DELETED,
                )
            } returns member
            every { memberRepository.save(capture(memberSlot)) } returns member
            every { eventPublisher.publishEvent(capture(eventSlot)) } just Runs

            When("마지막 접속 시간이 업데이트 될 때") {
                memberService.putLastAccessTime(email, IPADDRESS, USER_AGENT, IDEMPOTENCYKEY)

                Then("회원의 마지막 접속 시간이 업데이트되어야 한다.") {
                    val savedMember = memberSlot.captured
                    savedMember.lastAccessTime shouldBe after(beforeTime)
                }

                Then("회원 로그 이벤트가 발생한다") {
                    val event = eventSlot.captured as MemberLoginEvent
                    event.memberId shouldBe member.formattedId
                    event.idemPotencyKey shouldBe IDEMPOTENCYKEY
                    event.ipAddress shouldBe IPADDRESS
                    event.userAgent shouldBe USER_AGENT
                }
            }

            When("존재하지 않은 이메일로 서비스가 호출되면") {
                every {
                    memberRepository.findMemberByMemberEmailAndMemberStatusNot(
                        wrongEmail,
                        MemberStatus.ROLE_DELETED,
                    )
                } returns null

                Then("존재하지 않는 회원 예외를 반환한다.") {
                    val exception =
                        shouldThrow<CommonException> {
                            memberService.putLastAccessTime(wrongEmail, IPADDRESS, USER_AGENT, IDEMPOTENCYKEY)
                        }
                    exception.errorCode shouldBe ErrorCode.NOT_FOUND_MEMBER
                }
            }

            When("마지막 접속 시간 저장 과정에서 오류가 발생하면") {
                every {
                    memberRepository.findMemberByMemberEmailAndMemberStatusNot(
                        email,
                        MemberStatus.ROLE_DELETED,
                    )
                } returns (member)
                every { memberRepository.save(any()) } throws DataIntegrityViolationException("Database Error")

                Then("서버 오류 예외가 발생한다.") {
                    val exception =
                        shouldThrow<CommonException> {
                            memberService.putLastAccessTime(email, IPADDRESS, USER_AGENT, IDEMPOTENCYKEY)
                        }
                    exception.errorCode shouldBe ErrorCode.INTERNAL_SERVER_ERROR
                }
            }
        }

        // 프로필 업데이트 테스트(프로필 사진 포함 x)
        Given("요청에 프로필 사진이 없는 사용자가") {
            val memberId = 1L
            val formattedId = createFormattedId()
            val member = createMember()
            val requestDTO =
                createProfileRequestDTO(
                    CHANGE_NICKNAME,
                    GENDER,
                    LocalDate.of(1990, 1, 1),
                    INTRODUCTION,
                )
            val idempotencyKey = IDEMPOTENCYKEY
            val memberSlot = slot<Member>()
            val eventSlot = slot<Any>()
            val outBoxSlot = slot<OutboxDTO>()
            every {
                memberRepository.findMemberByMemberIdAndMemberStatusNot(
                    memberId,
                    MemberStatus.ROLE_DELETED,
                )
            } returns member
            every { memberRepository.save(capture(memberSlot)) } returns member
            every { eventPublisher.publishEvent(any()) } just Runs
            every { outBoxService.createOutbox(any()) } just Runs

            When("프로필 변경을 요청하면") {
                memberService.patchProfile(formattedId, requestDTO, null, idempotencyKey)

                Then("파일 서비스는 호출되지 않는다") {
                    verify(exactly = 0) {
                        commandFileService.createSingleFile(any(), any())
                        commandFileService.updateProfileImage(any(), any())
                    }
                }

                Then("사진을 제외한 프로필이 변경된다") {
                    verify(exactly = 1) { memberRepository.save(any()) }
                    val changedMember = memberSlot.captured
                    changedMember.memberNickname shouldBe CHANGE_NICKNAME
                    changedMember.memberBirth shouldBe LocalDate.of(1990, 1, 1)
                    changedMember.memberGender shouldBe GENDER
                    changedMember.selfIntroduction shouldBe INTRODUCTION
                }

                Then("회원 변경 로그 이벤트가 발행된다") {
                    verify(exactly = 1) { eventPublisher.publishEvent(capture(eventSlot)) }
                    val event = eventSlot.captured as MemberUpdateEvent
                    event.memberId shouldBe formattedId
                    event.memberNickname shouldBe CHANGE_NICKNAME
                    event.memberBirth shouldBe LocalDate.of(1990, 1, 1)
                    event.memberGender shouldBe GENDER
                }

                Then("Outbox가 생성된다") {
                    verify(exactly = 1) { outBoxService.createOutbox(capture(outBoxSlot)) }
                    val outBox = outBoxSlot.captured
                    outBox.aggregateId shouldBe formattedId
                    outBox.eventType shouldBe EventType.MEMBER
                    outBox.idempotencyKey shouldBe IDEMPOTENCYKEY

                    val payLoad = objectMapper.readValue(outBox.payload, MemberUpdateEvent::class.java)
                    payLoad.memberGender shouldBe GENDER
                    payLoad.memberBirth shouldBe LocalDate.of(1990, 1, 1)
                    payLoad.memberNickname shouldBe CHANGE_NICKNAME
                    payLoad.memberId shouldBe formattedId
                }
            }
        }

        // 프로필 변경 요청 테스트(사진 포함 o, 기본 프사)
        Given("기본 프사인 사용자가") {
            val memberId = 1L
            val formattedId = createFormattedId()
            val member = createMember().apply { memberPhoto = "" }
            val requestDTO =
                createProfileRequestDTO(
                    CHANGE_NICKNAME,
                    GENDER,
                    LocalDate.of(1990, 1, 1),
                    INTRODUCTION,
                )
            val idempotencyKey = IDEMPOTENCYKEY
            val memberSlot = slot<Member>()
            val file = mockk<MultipartFile>(relaxed = true)
            val profileURL = "new-url"
            every {
                memberRepository.findMemberByMemberIdAndMemberStatusNot(
                    memberId,
                    MemberStatus.ROLE_DELETED,
                )
            } returns member
            every { eventPublisher.publishEvent(any()) } just Runs
            every { commandFileService.createSingleFile(any(), any()) } returns profileURL
            every { memberRepository.save(capture(memberSlot)) } returns member
            every { outBoxService.createOutbox(any()) } just Runs

            When("프로필 변경을 요청하면") {
                memberService.patchProfile(formattedId, requestDTO, file, idempotencyKey)

                Then("createSingleFile method가 호출된다") {
                    verify(exactly = 1) {
                        commandFileService.createSingleFile(any(), any())
                    }
                }

                Then("updateProfileImage method는 호출되지 않는다") {
                    verify(exactly = 0) {
                        commandFileService.updateProfileImage(any(), any())
                    }
                }

                Then("회원의 프로필 사진이 변경된다") {
                    verify(exactly = 1) { memberRepository.save(any()) }
                    val changedMember = memberSlot.captured
                    changedMember.memberPhoto shouldBe profileURL
                }
            }
        }

        // 프로필 변경 요청 테스트(사진 포함 o, 기존 프사 o)
        Given("기존 프사가 있는 사용자가") {
            val memberId = 1L
            val formattedId = createFormattedId()
            val member = createMember().apply { memberPhoto = "not_blank" }
            val requestDTO =
                createProfileRequestDTO(
                    CHANGE_NICKNAME,
                    GENDER,
                    LocalDate.of(1990, 1, 1),
                    INTRODUCTION,
                )
            val idempotencyKey = IDEMPOTENCYKEY
            val memberSlot = slot<Member>()
            val file = mockk<MultipartFile>()
            val profileURL = "new-url"
            every {
                memberRepository.findMemberByMemberIdAndMemberStatusNot(
                    memberId,
                    MemberStatus.ROLE_DELETED,
                )
            } returns member
            every { eventPublisher.publishEvent(any()) } just Runs
            every { commandFileService.updateProfileImage(any(), any()) } returns profileURL
            every { memberRepository.save(capture(memberSlot)) } returns member
            every { outBoxService.createOutbox(any()) } just Runs

            When("프로필 변경을 요청하면") {
                memberService.patchProfile(formattedId, requestDTO, file, idempotencyKey)

                Then("createSingleFile method는 호출되지 않는다") {
                    verify(exactly = 0) {
                        commandFileService.createSingleFile(any(), any())
                    }
                }

                Then("updateProfileImage method가 호출된다") {
                    verify(exactly = 1) {
                        commandFileService.updateProfileImage(any(), any())
                    }
                }

                Then("회원의 프로필 사진이 변경된다") {
                    verify(exactly = 1) { memberRepository.save(any()) }
                    val changedMember = memberSlot.captured
                    changedMember.memberPhoto shouldBe profileURL
                }
            }
        }

        // 회원 프로필 변경 실패 테스트(존재하지 않는 회원 번호)
        Given("존재하지 않는 회원이") {
            val formattedId = createFormattedId()
            val requestDTO =
                createProfileRequestDTO(
                    CHANGE_NICKNAME,
                    GENDER,
                    LocalDate.of(1990, 1, 1),
                    INTRODUCTION,
                )
            val idempotencyKey = IDEMPOTENCYKEY
            every {
                memberRepository.findMemberByMemberIdAndMemberStatusNot(
                    1L,
                    MemberStatus.ROLE_DELETED,
                )
            } returns null

            When("프로필 변경을 요청하면") {
                Then("존재하지 않는 회원 예외를 반환한다") {
                    val exception =
                        shouldThrow<CommonException> {
                            memberService.patchProfile(formattedId, requestDTO, null, idempotencyKey)
                        }

                    exception.errorCode shouldBe ErrorCode.NOT_FOUND_MEMBER
                }
            }
        }

        // 회원 프로필 변경 실패 테스트(DB저장 실패)
        Given("일반 회원이") {
            val memberId = 1L
            val formattedId = createFormattedId()
            val member = createMember()
            val requestDTO =
                createProfileRequestDTO(
                    CHANGE_NICKNAME,
                    GENDER,
                    LocalDate.of(1990, 1, 1),
                    INTRODUCTION,
                )
            val idempotencyKey = IDEMPOTENCYKEY
            val memberSlot = slot<Member>()
            every {
                memberRepository.findMemberByMemberIdAndMemberStatusNot(
                    memberId,
                    MemberStatus.ROLE_DELETED,
                )
            } returns member
            every { eventPublisher.publishEvent(any()) } just Runs

            When("프로필 변경을 요청할 때 DB 저장에 실패하면") {
                every { memberRepository.save(capture(memberSlot)) } throws DataIntegrityViolationException("Database Error")

                Then("서버 내부 오류 예외를 반환한다") {
                    val exception =
                        shouldThrow<CommonException> {
                            memberService.patchProfile(formattedId, requestDTO, null, idempotencyKey)
                        }

                    exception.errorCode shouldBe ErrorCode.INTERNAL_SERVER_ERROR
                }
            }
        }

        // 회원 탈퇴 테스트
        Given("회원이 탈퇴를 요청하면") {
            val memberId = 1L
            val formattedId = createFormattedId()
            val member = createMember()
            val idempotencyKey = IDEMPOTENCYKEY
            val memberSlot = slot<Member>()
            val eventSlot = slot<Any>()
            val outBoxSlot = slot<OutboxDTO>()
            every { eventPublisher.publishEvent(capture(eventSlot)) } just Runs
            every {
                memberRepository.findMemberByMemberIdAndMemberStatusNot(
                    memberId,
                    MemberStatus.ROLE_DELETED,
                )
            } returns member
            every { memberRepository.save(capture(memberSlot)) } returns member
            every { outBoxService.createOutbox(capture(outBoxSlot)) } just Runs

            Then("회원 탈퇴가 된다") {
                memberService.deleteMember(formattedId, idempotencyKey)

                val deletedMember = memberSlot.captured
                deletedMember.memberStatus shouldBe MemberStatus.ROLE_DELETED
            }

            Then("회원 탈퇴 로그 이벤트가 발행된다") {

                val event = eventSlot.captured as MemberDeleteEvent
                event.memberId shouldBe formattedId
                event.memberEmail shouldBe member.memberEmail
            }

            Then("Outbox가 생성된다") {

                val outBox = outBoxSlot.captured
                outBox.aggregateId shouldBe formattedId
                outBox.idempotencyKey shouldBe IDEMPOTENCYKEY
                outBox.eventType shouldBe EventType.MEMBER

                val payLoad = objectMapper.readValue(outBox.payload, MemberDeleteEvent::class.java)
                payLoad.memberEmail shouldBe member.memberEmail
                payLoad.memberId shouldBe formattedId
            }
        }

        // 회원 탈퇴 실패 테스트(존재하지 않는 회원)
        Given("존재 안 하는 회원이") {
            val memberId = 1L
            val formattedId = createFormattedId()
            val idempotencyKey = IDEMPOTENCYKEY
            every {
                memberRepository.findMemberByMemberIdAndMemberStatusNot(
                    memberId,
                    MemberStatus.ROLE_DELETED,
                )
            } returns null

            When("회원 탈퇴 요청을 하면") {
                Then("존재하지 않는 회원 예외가 반환된다") {
                    val exception =
                        shouldThrow<CommonException> {
                            memberService.deleteMember(formattedId, idempotencyKey)
                        }

                    exception.errorCode shouldBe ErrorCode.NOT_FOUND_MEMBER
                }
            }
        }
    })