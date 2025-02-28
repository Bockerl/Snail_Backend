@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.area.command.domain.aggregate.entity.ActivityArea
import com.bockerl.snailmember.area.command.domain.aggregate.entity.AreaType
import com.bockerl.snailmember.area.command.domain.repository.ActivityAreaRepository
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.command.domain.aggregate.entity.Member
import com.bockerl.snailmember.member.command.domain.aggregate.entity.SignUpPath
import com.bockerl.snailmember.member.command.domain.aggregate.entity.tempMember.SignUpStep
import com.bockerl.snailmember.member.command.domain.aggregate.entity.tempMember.VerificationType
import com.bockerl.snailmember.member.command.domain.repository.MemberRepository
import com.bockerl.snailmember.member.command.domain.repository.TempMemberRepository
import com.bockerl.snailmember.member.command.domain.service.RegistrationServiceImpl
import com.bockerl.snailmember.utils.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class RegistrationServiceImplTests :
    BehaviorSpec({
        // mock 설정
        val authService = mockk<AuthService>()
        val tempRepository = mockk<TempMemberRepository>()
        val memberRepository = mockk<MemberRepository>()
        val activityAreaRepository = mockk<ActivityAreaRepository>()
        val bcryptPasswordEncoder = mockk<BCryptPasswordEncoder>()

        // test 서비스 설정
        val registrationService = RegistrationServiceImpl(
            authService = authService,
            tempRepository,
            memberRepository = memberRepository,
            activityAreaRepository = activityAreaRepository,
            bcryptPasswordEncoder = bcryptPasswordEncoder,
        )

        // 1. 회원가입 초기화 테스트
        Given("새로운 사용자가 이메일 회원가입을 하려고 할 때") {
            val request = createEmailRequestDTO()
            every { tempRepository.save(any()) } returns TEST_REDIS_ID
            every { memberRepository.findMemberByMemberEmail(request.memberEmail) } returns null
            every { authService.createEmailVerificationCode(TEST_EMAIL) } just Runs

            When("유효한 이메일과 닉네임으로 회원가입 요청을 하면") {
                val result = registrationService.initiateRegistration(request)

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
                    verify { authService.createEmailVerificationCode(TEST_EMAIL) }
                }
            }

            When("이미 존재하는 이메일로 회원가입을 요청하면") {
                val existingMember = createMember(memberEmail = TEST_EMAIL)
                every { memberRepository.findMemberByMemberEmail(request.memberEmail) } returns existingMember

                Then("이미 존재하는 회원 예외를 반환한다.") {
                    val exception = shouldThrow<CommonException> {
                        registrationService.initiateRegistration(request)
                    }
                    exception.errorCode shouldBe ErrorCode.EXIST_USER
                }
            }
        }

        // 2. 이메일 인증 코드 재요청 테스트
        Given("사용자가 이메일 코드를 재요청할 때") {
            val redisId = TEST_REDIS_ID
            val tempMember = createInitialTempMember()
            every { tempRepository.find(redisId) } returns tempMember
            every { authService.createEmailVerificationCode(tempMember.email) } just Runs

            When("유효한 RedisId를 제공하면") {
                registrationService.createEmailRefreshCode(redisId)

                Then("새로운 이메일 인증코드가 생성된다.") {
                    verify { authService.createEmailVerificationCode(tempMember.email) }
                }
            }

            When("잘못된 순서로 코드를 재요청하면") {
                val wrongTempMember = tempMember.copy(signUpStep = SignUpStep.EMAIL_VERIFIED)
                every { tempRepository.find(redisId) } returns wrongTempMember

                Then("잘못된 권한 예외가 발생해야 한다.") {
                    val exception = shouldThrow<CommonException> {
                        registrationService.createEmailRefreshCode(redisId)
                    }
                    exception.errorCode shouldBe ErrorCode.UNAUTHORIZED_ACCESS
                }
            }

            When("임시회원의 세션이 만료되면") {
                every { tempRepository.find(redisId) } returns null

                Then("세션 만료 예외가 발생해야 한다.") {
                    val exception = shouldThrow<CommonException> {
                        registrationService.createEmailRefreshCode(redisId)
                    }
                    exception.errorCode shouldBe ErrorCode.EXPIRED_SIGNUP_SESSION
                }
            }
        }

        // 3. 이메일 인증 요청 테스트
        Given("사용자가 이메일 인증을 요청할 때") {
            val request = createEmailVerifyRequestDTO()
            val tempMember = createTempMember()

            When("유효한 인증 코드를 입력하면") {
                every { tempRepository.find(TEST_REDIS_ID) } returns tempMember
                every { authService.verifyCode(TEST_EMAIL, VERIFICATION_CODE, VerificationType.EMAIL) } just Runs
                every { tempRepository.update(any(), any()) } just Runs

                val result = registrationService.verifyEmailCode(request)

                Then("RedisId가 반환되어야 한다.") {
                    result shouldBe TEST_REDIS_ID
                }

                Then("인증 코드가 검증되어야 한다.") {
                    verify { authService.verifyCode(TEST_EMAIL, VERIFICATION_CODE, VerificationType.EMAIL) }
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
                    val exception = shouldThrow<CommonException> {
                        registrationService.verifyEmailCode(request)
                    }
                    exception.errorCode shouldBe ErrorCode.EXPIRED_SIGNUP_SESSION
                }
            }

            When("잘못된 순서로 이메일 인증을 하면") {
                val wrongTempMember = tempMember.copy(signUpStep = SignUpStep.EMAIL_VERIFIED)
                every { tempRepository.find(TEST_REDIS_ID) } returns wrongTempMember

                Then("잘못된 권한 예외가 발생한다.") {
                    val exception = shouldThrow<CommonException> {
                        registrationService.verifyEmailCode(request)
                    }
                    exception.errorCode shouldBe ErrorCode.UNAUTHORIZED_ACCESS
                }
            }
        }

        // 휴대폰 인증 코드 생성
        Given("이메일을 인증한 사용자가") {
            val request = createPhoneRequestDTO()
            val tempMember = createTempMember(
                signUpStep = SignUpStep.EMAIL_VERIFIED,
            )

            When("핸드폰 인증 코드 생성을 요청하면") {
                every { tempRepository.find(request.redisId) } returns tempMember
                every { authService.createPhoneVerificationCode(request.phoneNumber) } returns VERIFICATION_CODE
                every { tempRepository.update(any(), any()) } just Runs

                val result = registrationService.createPhoneVerificationCode(request)

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
                    val exception = shouldThrow<CommonException> {
                        registrationService.createPhoneVerificationCode(request)
                    }
                    exception.errorCode shouldBe ErrorCode.EXPIRED_SIGNUP_SESSION
                }
            }

            When("잘못된 순서로 휴대폰 인증 코드 생성 요청을 하면") {
                val wrongTempMember = tempMember.copy(
                    signUpStep = SignUpStep.PHONE_VERIFIED,
                )
                every { tempRepository.find(TEST_REDIS_ID) } returns wrongTempMember

                Then("잘못된 권한 예외가 발생한다.") {
                    val exception = shouldThrow<CommonException> {
                        registrationService.createPhoneVerificationCode(request)
                    }

                    exception.errorCode shouldBe ErrorCode.UNAUTHORIZED_ACCESS
                }
            }
        }

        // 휴대폰 인증 코드 재요청 테스트
        Given("사용자가 휴대폰 인증 코드를 재요청할 때") {
            val redisId = TEST_REDIS_ID
            val tempMember = createTempMember(
                signUpStep = SignUpStep.EMAIL_VERIFIED,
            )
            every { tempRepository.find(redisId) } returns tempMember
            every { authService.createPhoneVerificationCode(tempMember.phoneNumber) } returns VERIFICATION_CODE

            When("유효한 RedisId를 제공하면") {
                registrationService.createPhoneRefreshCode(redisId)

                Then("새로운 인증 코드가 생성된다.") {
                    verify { authService.createPhoneVerificationCode(tempMember.phoneNumber) }
                }
            }

            When("임시회원의 세션이 만료되면") {
                every { tempRepository.find(redisId) } returns null

                Then("세션 만료 예외가 발생한다.") {
                    val exception = shouldThrow<CommonException> {
                        registrationService.createPhoneRefreshCode(redisId)
                    }
                    exception.errorCode shouldBe ErrorCode.EXPIRED_SIGNUP_SESSION
                }
            }

            When("잘못된 순서로 요청을 하면") {
                val wrongTempMember = tempMember.copy(
                    signUpStep = SignUpStep.PHONE_VERIFIED,
                )
                every { tempRepository.find(redisId) } returns wrongTempMember

                Then("잘못된 권한 예외가 발생한다.") {
                    val exception = shouldThrow<CommonException> {
                        registrationService.createPhoneRefreshCode(redisId)
                    }
                    exception.errorCode shouldBe ErrorCode.UNAUTHORIZED_ACCESS
                }
            }
        }

        // 휴대폰 인증 요청 테스트
        Given("휴대폰 인증 요청한 사용자가") {
            val request = createPhoneVerifyRequestDTO()
            val tempMember = createTempMember(
                signUpStep = SignUpStep.EMAIL_VERIFIED,
            )

            When("유효한 코드로 휴대폰 인증을 하면") {
                every { tempRepository.find(request.redisId) } returns tempMember
                every {
                    authService.verifyCode(tempMember.phoneNumber, request.verificationCode, VerificationType.PHONE)
                } just Runs
                every { tempRepository.update(any(), any()) } just Runs
                val result = registrationService.verifyPhoneCode(request)

                Then("RedisId가 반환되어야 한다.") {
                    result shouldBe TEST_REDIS_ID
                }

                Then("인증 코드가 검증되어야 한다.") {
                    verify {
                        authService.verifyCode(tempMember.phoneNumber, request.verificationCode, VerificationType.PHONE)
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

        // 비밀번호 입력 테스트
        Given("휴대폰을 인증한 사용자가") {
            val request = createPassWordRequestDTO()
            val tempMember = createTempMember(
                signUpStep = SignUpStep.PHONE_VERIFIED,
            )

            When("비밀번호 입력을 하면") {
                every { tempRepository.find(request.redisId) } returns tempMember
                every { tempRepository.update(any(), any()) } just Runs

                val result = registrationService.postPassword(request)

                Then("RedisId를 반환한다.") {
                    result shouldBe TEST_REDIS_ID
                }

                Then("임시회원에 비밀번호가 입력된다.") {
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

            When("임시회원 세션이 만료되면") {
                every { tempRepository.find(request.redisId) } returns null

                Then("세션 만료 예외를 반환한다.") {
                    val exception = shouldThrow<CommonException> {
                        registrationService.postPassword(request)
                    }
                    exception.errorCode shouldBe ErrorCode.EXPIRED_SIGNUP_SESSION
                }
            }
        }

        // 활동지역 등록(회원가입 완료) 테스트
        Given("비밀번호를 입력한 사용자가") {
            val request = createActivityAreaRequestDTO()
            val tempMember = createTempMember(
                signUpStep = SignUpStep.PASSWORD_VERIFIED,
            )
            val memberSlot = slot<Member>()

            When("활동지역을 등록하고 회원가입을 완료하면") {
                every { tempRepository.find(request.redisId) } returns tempMember
                every { bcryptPasswordEncoder.encode(any()) } returns TEST_PASSWORD
                every { memberRepository.save(capture(memberSlot)) } answers {
                    memberSlot.captured.apply { memberId = 1L }
                }
                every { activityAreaRepository.save((any())) } answers { firstArg() }
                every { tempRepository.delete(request.redisId) } just Runs

                registrationService.postActivityArea(request)

                Then("새 회원이 데이터베이스에 저장된다.") {
                    verify { memberRepository.save(any()) }
                    memberSlot.captured.memberId shouldBe 1L
                    memberSlot.captured.memberEmail shouldBe TEST_EMAIL
                    memberSlot.captured.memberPassword shouldBe TEST_PASSWORD
                    memberSlot.captured.memberNickName shouldBe TEST_NICKNAME
                    memberSlot.captured.memberPhoneNumber shouldBe TEST_PHONE
                    memberSlot.captured.signupPath shouldBe SignUpPath.EMAIL
                }

                Then("주 활동지역이 저장된다.") {
                    verify {
                        activityAreaRepository.save(
                            match<ActivityArea> {
                                it.areaType == AreaType.PRIMARY &&
                                    it.id?.memberId == 1L &&
                                    it.id?.emdAreasId == 1L
                            },
                        )
                    }
                }

                Then("직장 활동지역이 저장된다.") {
                    verify {
                        activityAreaRepository.save(
                            match<ActivityArea> {
                                it.areaType == AreaType.WORKPLACE &&
                                    it.id?.memberId == 1L &&
                                    it.id?.emdAreasId == 2L
                            },
                        )
                    }
                }

                Then("임시 회원 객체가 삭제된다.") {
                    verify { tempRepository.delete(request.redisId) }
                }
            }

            When("주 활동지역과 직장 활동지역이 일치하면") {
                val request = createActivityAreaRequestDTO(
                    workplaceArea = TEST_PRIMARY_AREA,
                )

                every { tempRepository.find(request.redisId) } returns tempMember
                every { memberRepository.save(any()) } answers { firstArg<Member>().apply { memberId = 1L } }

                Then("권한 없음 예외가 발생한다.") {
                    val exception = shouldThrow<CommonException> {
                        registrationService.postActivityArea(request)
                    }
                    exception.errorCode shouldBe ErrorCode.UNAUTHORIZED_ACCESS
                }
            }
        }
    })
