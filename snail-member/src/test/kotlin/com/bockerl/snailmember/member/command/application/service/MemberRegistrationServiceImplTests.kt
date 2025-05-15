@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.infrastructure.outbox.dto.OutboxDTO
import com.bockerl.snailmember.infrastructure.outbox.enums.EventType
import com.bockerl.snailmember.infrastructure.outbox.service.OutboxService
import com.bockerl.snailmember.member.command.domain.aggregate.entity.Member
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.MemberStatus
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.SignUpPath
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.SignUpStep
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.VerificationType
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberCreateEvent
import com.bockerl.snailmember.member.command.domain.repository.MemberRepository
import com.bockerl.snailmember.member.command.domain.repository.TempMemberRepository
import com.bockerl.snailmember.member.command.domain.service.MemberRegistrationServiceImpl
import com.bockerl.snailmember.utils.*
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class MemberRegistrationServiceImplTests :
    BehaviorSpec({
        val logger = KotlinLogging.logger {}
        // mock 설정
        val memberAuthService = mockk<MemberAuthService>()
        val tempRepository = mockk<TempMemberRepository>()
        val memberRepository = mockk<MemberRepository>()
        val bcryptPasswordEncoder = mockk<BCryptPasswordEncoder>()
        val outBoxService = mockk<OutboxService>()
        val eventPublisher = mockk<ApplicationEventPublisher>()
        val redisTemplate = mockk<RedisTemplate<String, String>>()
        val objectMapper =
            ObjectMapper().apply {
                registerModule(KotlinModule.Builder().build())
                registerModule(JavaTimeModule())
                enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }

        // test 서비스 설정
        val registrationService =
            MemberRegistrationServiceImpl(
                memberAuthService = memberAuthService,
                tempMemberRepository = tempRepository,
                memberRepository = memberRepository,
                bcryptPasswordEncoder = bcryptPasswordEncoder,
                outboxService = outBoxService,
                eventPublisher = eventPublisher,
                objectMapper = objectMapper,
                redisTemplate = redisTemplate,
            )

        var cnt = 0
        beforeTest {
            val valOps = mockk<ValueOperations<String, String>>()
            every { redisTemplate.opsForValue() } returns valOps
            every { valOps.get(IDEMPOTENCYKEY) } returns null
            every { valOps.set(IDEMPOTENCYKEY, any()) } answers {
                cnt += 1
            }
        }

        // 1. 회원가입 초기화 테스트
        Given("새로운 사용자가 이메일 회원가입을 하려고 할 때") {
            val request = createEmailRequestDTO()
            val idempotencyKey = createIdempotencyKey()
            every { tempRepository.save(any()) } returns TEST_REDIS_ID
            every {
                memberRepository.findMemberByMemberEmailAndMemberStatusNot(
                    request.memberEmail,
                    MemberStatus.ROLE_DELETED,
                )
            } returns
                null
            every { memberAuthService.createEmailVerificationCode(TEST_EMAIL) } just Runs

            When("유효한 이메일과 닉네임으로 회원가입 요청을 하면") {
                val result = registrationService.initiateRegistration(request, idempotencyKey)

                Then("Redis ID가 반환된다.") {
                    result shouldBe TEST_REDIS_ID
                }

                Then("임시 회원이 Redis에 저장되어야 한다.") {
                    verify {
                        tempRepository.save(
                            match { tempMember ->
                                tempMember.email == TEST_EMAIL &&
                                    tempMember.nickName == TEST_NICKNAME &&
                                    tempMember.birth == TEST_BIRTH &&
                                    tempMember.signUpStep == SignUpStep.INITIAL
                            },
                        )
                    }
                }

                Then("이메일 인증코드가 생성된다.") {
                    verify { memberAuthService.createEmailVerificationCode(TEST_EMAIL) }
                }
            }

            When("이미 존재하는 이메일로 회원가입을 요청하면") {
                val existingMember = createMember(memberEmail = TEST_EMAIL)
                every {
                    memberRepository.findMemberByMemberEmailAndMemberStatusNot(
                        eq(TEST_EMAIL),
                        MemberStatus.ROLE_DELETED,
                    )
                } returns existingMember

                Then("이미 존재하는 회원 예외를 반환한다.") {
                    val exception =
                        shouldThrow<CommonException> {
                            registrationService.initiateRegistration(request, idempotencyKey)
                        }
                    exception.errorCode shouldBe ErrorCode.EXIST_USER
                }
            }
        }

        // 2. 이메일 인증 코드 재요청 테스트
        Given("사용자가 이메일 코드를 재요청할 때") {
            val redisId = TEST_REDIS_ID
            val tempMember = createInitialTempMember()
            val idempotencyKey = createIdempotencyKey()
            every { tempRepository.find(redisId) } returns tempMember

            When("유효한 RedisId를 제공하면") {
                registrationService.createEmailRefreshCode(redisId, idempotencyKey)

                Then("새로운 이메일 인증코드가 생성된다.") {
                    verify { memberAuthService.createEmailVerificationCode(tempMember.email) }
                }
            }

            When("잘못된 순서로 코드를 재요청하면") {
                val wrongTempMember = tempMember.copy(signUpStep = SignUpStep.EMAIL_VERIFIED)
                every { tempRepository.find(redisId) } returns wrongTempMember

                Then("잘못된 권한 예외가 발생해야 한다.") {
                    val exception =
                        shouldThrow<CommonException> {
                            registrationService.createEmailRefreshCode(redisId, idempotencyKey)
                        }
                    exception.errorCode shouldBe ErrorCode.UNAUTHORIZED_ACCESS
                }
            }

            When("임시회원의 세션이 만료되면") {
                every { tempRepository.find(redisId) } returns null

                Then("세션 만료 예외가 발생해야 한다.") {
                    val exception =
                        shouldThrow<CommonException> {
                            registrationService.createEmailRefreshCode(redisId, idempotencyKey)
                        }
                    exception.errorCode shouldBe ErrorCode.EXPIRED_SIGNUP_SESSION
                }
            }
        }

        // 3. 이메일 인증 요청 테스트
        Given("사용자가 이메일 인증을 요청할 때") {
            val request = createEmailVerifyRequestDTO()
            val tempMember = createTempMember()
            val idempotencyKey = createIdempotencyKey()
            When("유효한 인증 코드를 입력하면") {
                every { tempRepository.find(TEST_REDIS_ID) } returns tempMember
                every { memberAuthService.verifyCode(TEST_EMAIL, VERIFICATION_CODE, VerificationType.EMAIL) } just Runs
                every { tempRepository.update(any(), any()) } just Runs

                val result = registrationService.verifyEmailCode(request, idempotencyKey)

                Then("RedisId가 반환되어야 한다.") {
                    result shouldBe TEST_REDIS_ID
                }

                Then("인증 코드가 검증되어야 한다.") {
                    verify { memberAuthService.verifyCode(TEST_EMAIL, VERIFICATION_CODE, VerificationType.EMAIL) }
                }

                Then("임시 회원의 상태가 이메일 인증으로 바뀌어야 한다.") {
                    verify {
                        tempRepository.update(
                            eq(TEST_REDIS_ID),
                            match { updatedTempMember ->
                                updatedTempMember.signUpStep == SignUpStep.EMAIL_VERIFIED
                            },
                        )
                    }
                }
            }

            When("임시회원의 세션이 만료되면") {
                every { tempRepository.find(TEST_REDIS_ID) } returns null

                Then("세션 만료 예외가 발생해야 한다.") {
                    val exception =
                        shouldThrow<CommonException> {
                            registrationService.verifyEmailCode(request, idempotencyKey)
                        }
                    exception.errorCode shouldBe ErrorCode.EXPIRED_SIGNUP_SESSION
                }
            }

            When("잘못된 순서로 이메일 인증을 하면") {
                val wrongTempMember = tempMember.copy(signUpStep = SignUpStep.EMAIL_VERIFIED)
                every { tempRepository.find(TEST_REDIS_ID) } returns wrongTempMember

                Then("잘못된 권한 예외가 발생한다.") {
                    val exception =
                        shouldThrow<CommonException> {
                            registrationService.verifyEmailCode(request, idempotencyKey)
                        }
                    exception.errorCode shouldBe ErrorCode.UNAUTHORIZED_ACCESS
                }
            }
        }

        // 휴대폰 인증 코드 생성
        Given("이메일을 인증한 사용자가") {
            val request = createPhoneRequestDTO()
            val tempMember =
                createTempMember(
                    signUpStep = SignUpStep.EMAIL_VERIFIED,
                )
            val idempotencyKey = createIdempotencyKey()
            When("핸드폰 인증 코드 생성을 요청하면") {
                every { tempRepository.find(request.redisId) } returns tempMember
                every { memberAuthService.createPhoneVerificationCode(request.phoneNumber) } returns VERIFICATION_CODE
                every { tempRepository.update(any(), any()) } just Runs

                val result = registrationService.createPhoneVerificationCode(request, idempotencyKey)

                Then("인증코드가 반환되어야 한다.") {
                    result shouldBe VERIFICATION_CODE
                }

                Then("임시회원에 휴대폰 번호가 저장되고 상태가 변경된다.") {
                    verify {
                        tempRepository.update(
                            eq(TEST_REDIS_ID),
                            match { updatedTempMember ->
                                updatedTempMember.phoneNumber == TEST_PHONE
                            },
                        )
                    }
                }
            }

            When("임시회원의 세션이 만료되면") {
                every { tempRepository.find(request.redisId) } returns null

                Then("세션 만료 예외가 발생한다.") {
                    val exception =
                        shouldThrow<CommonException> {
                            registrationService.createPhoneVerificationCode(request, idempotencyKey)
                        }
                    exception.errorCode shouldBe ErrorCode.EXPIRED_SIGNUP_SESSION
                }
            }

            When("잘못된 순서로 휴대폰 인증 코드 생성 요청을 하면") {
                val wrongTempMember =
                    tempMember.copy(
                        signUpStep = SignUpStep.PHONE_VERIFIED,
                    )
                every { tempRepository.find(TEST_REDIS_ID) } returns wrongTempMember

                Then("잘못된 권한 예외가 발생한다.") {
                    val exception =
                        shouldThrow<CommonException> {
                            registrationService.createPhoneVerificationCode(request, idempotencyKey)
                        }

                    exception.errorCode shouldBe ErrorCode.UNAUTHORIZED_ACCESS
                }
            }
        }

        // 휴대폰 인증 코드 재요청 테스트
        Given("사용자가 휴대폰 인증 코드를 재요청할 때") {
            val redisId = TEST_REDIS_ID
            val tempMember =
                createTempMember(
                    signUpStep = SignUpStep.EMAIL_VERIFIED,
                )
            val phoneRequestDTO = createPhoneRequestDTO()
            val idempotencyKey = createIdempotencyKey()
            every { tempRepository.find(redisId) } returns tempMember
            every { memberAuthService.createPhoneVerificationCode(tempMember.phoneNumber) } returns VERIFICATION_CODE

            When("유효한 RedisId를 제공하면") {
                registrationService.createPhoneRefreshCode(phoneRequestDTO, idempotencyKey)

                Then("새로운 인증 코드가 생성된다.") {
                    verify { memberAuthService.createPhoneVerificationCode(tempMember.phoneNumber) }
                }
            }

            When("임시회원의 세션이 만료되면") {
                every { tempRepository.find(redisId) } returns null

                Then("세션 만료 예외가 발생한다.") {
                    val exception =
                        shouldThrow<CommonException> {
                            registrationService.createPhoneRefreshCode(phoneRequestDTO, idempotencyKey)
                        }
                    exception.errorCode shouldBe ErrorCode.EXPIRED_SIGNUP_SESSION
                }
            }

            When("잘못된 순서로 요청을 하면") {
                val wrongTempMember =
                    tempMember.copy(
                        signUpStep = SignUpStep.PHONE_VERIFIED,
                    )
                every { tempRepository.find(redisId) } returns wrongTempMember

                Then("잘못된 권한 예외가 발생한다.") {
                    val exception =
                        shouldThrow<CommonException> {
                            registrationService.createPhoneRefreshCode(phoneRequestDTO, idempotencyKey)
                        }
                    exception.errorCode shouldBe ErrorCode.UNAUTHORIZED_ACCESS
                }
            }
        }

        // 휴대폰 인증 요청 테스트
        Given("휴대폰 인증 요청한 사용자가") {
            val request = createPhoneVerifyRequestDTO()
            val tempMember =
                createTempMember(
                    signUpStep = SignUpStep.EMAIL_VERIFIED,
                )
            val idempotencyKey = createIdempotencyKey()

            When("유효한 코드로 휴대폰 인증을 하면") {
                every { tempRepository.find(request.redisId) } returns tempMember
                every {
                    memberAuthService.verifyCode(tempMember.phoneNumber, request.verificationCode, VerificationType.PHONE)
                } just Runs
                every { tempRepository.update(any(), any()) } just Runs
                val result = registrationService.verifyPhoneCode(request, idempotencyKey)

                Then("RedisId가 반환되어야 한다.") {
                    result shouldBe TEST_REDIS_ID
                }

                Then("인증 코드가 검증되어야 한다.") {
                    verify {
                        memberAuthService.verifyCode(
                            tempMember.phoneNumber,
                            request.verificationCode,
                            VerificationType.PHONE,
                        )
                    }
                }

                Then("임시 회원의 상태가 휴대폰 인증으로 바뀌어야 한다.") {
                    verify {
                        tempRepository.update(
                            eq(TEST_REDIS_ID),
                            match { updatedTempMember ->
                                updatedTempMember.phoneNumber == TEST_PHONE &&
                                    updatedTempMember.signUpStep == SignUpStep.PHONE_VERIFIED
                            },
                        )
                    }
                }
            }
        }

        // 비밀번호 입력 테스트(회원가입 완료)
        Given("휴대폰을 인증한 사용자가") {
            val request = createPassWordRequestDTO()
            val tempMember =
                createTempMember(
                    signUpStep = SignUpStep.PHONE_VERIFIED,
                )
            val idempotencyKey = createIdempotencyKey()
            val memberSlot = slot<Member>()
            val eventSlot = slot<Any>()
            val outBoxSlot = slot<OutboxDTO>()

            When("비밀번호 입력을 하고 회원가입을 마치면") {
                every { tempRepository.find(request.redisId) } returns tempMember
                every { bcryptPasswordEncoder.encode(any()) } returns TEST_PASSWORD
                every { memberRepository.save(capture(memberSlot)) } answers {
                    memberSlot.captured.apply { memberId = 1L }
                }
                every { tempRepository.delete(request.redisId) } just Runs
                every { eventPublisher.publishEvent(capture(eventSlot)) } just Runs
                every { outBoxService.createOutbox(any()) } just Runs

                registrationService.postPassword(request, idempotencyKey)

                Then("새 회원이 데이터베이스에 저장된다.") {
                    verify(exactly = 1) { memberRepository.save(any()) }
                    val member = memberSlot.captured
                    member.memberId shouldBe 1L
                    member.memberEmail shouldBe TEST_EMAIL
                    member.memberPassword shouldBe TEST_PASSWORD
                    member.memberNickname shouldBe TEST_NICKNAME
                    member.memberPhoneNumber shouldBe TEST_PHONE
                    member.signupPath shouldBe SignUpPath.EMAIL
                    member.memberStatus shouldBe MemberStatus.ROLE_TEMP
                }

                Then("임시 회원 객체가 삭제된다.") {
                    verify(exactly = 1) { tempRepository.delete(request.redisId) }
                }

                Then("회원 생성 로그 이벤트가 발행된다") {
                    verify(exactly = 1) {
                        eventPublisher.publishEvent(capture(eventSlot))
                    }

                    val event = eventSlot.captured as MemberCreateEvent
                    event.memberId shouldBe FORMATTED_ID
                    event.memberBirth shouldBe LOCAL_DATE_TEST_BIRTH
                    event.memberEmail shouldBe TEST_EMAIL
                    event.memberNickname shouldBe TEST_NICKNAME
                    event.memberPhoneNumber shouldBe TEST_PHONE
                    event.signUpPath shouldBe SignUpPath.EMAIL
                    event.memberStatus shouldBe MemberStatus.ROLE_TEMP
                }

                Then("Outbox 테이블에 저장된다") {
                    verify(exactly = 1) {
                        outBoxService.createOutbox(capture(outBoxSlot))
                    }

                    val outBox = outBoxSlot.captured
                    outBox.aggregateId shouldBe FORMATTED_ID
                    outBox.eventType shouldBe EventType.MEMBER
                    outBox.idempotencyKey shouldBe IDEMPOTENCYKEY

                    val jsonPayLoad = objectMapper.readValue(outBox.payload, MemberCreateEvent::class.java)
                    jsonPayLoad.memberId shouldBe FORMATTED_ID
                    jsonPayLoad.memberEmail shouldBe TEST_EMAIL
                    jsonPayLoad.memberNickname shouldBe TEST_NICKNAME
                    jsonPayLoad.memberPhoneNumber shouldBe TEST_PHONE
                    jsonPayLoad.signUpPath shouldBe SignUpPath.EMAIL
                    jsonPayLoad.memberStatus shouldBe MemberStatus.ROLE_TEMP
                }
            }

            When("임시회원 세션이 만료되면") {
                every { tempRepository.find(request.redisId) } returns null

                Then("세션 만료 예외를 반환한다.") {
                    val exception =
                        shouldThrow<CommonException> {
                            registrationService.postPassword(request, idempotencyKey)
                        }
                    exception.errorCode shouldBe ErrorCode.EXPIRED_SIGNUP_SESSION
                }
            }
        }
    })