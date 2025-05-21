@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.VerificationType
import com.bockerl.snailmember.member.command.domain.service.MemberAuthServiceImpl
import com.bockerl.snailmember.utils.*
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import java.time.Duration

class AuthServiceImplTests :
    BehaviorSpec({
        val logger = KotlinLogging.logger {}
        // mock
        val mailSender = mockk<JavaMailSender>()
        val redisTemplate = mockk<RedisTemplate<String, String>>()
        // test 구현체
        lateinit var authService: MemberAuthService

        beforeContainer {
            clearAllMocks(answers = false)
            authService =
                MemberAuthServiceImpl(
                    redisTemplate,
                    mailSender,
                )
        }

        // 이메일 인증코드 생성 요청 테스트
        Given("이메일 회원가입 사용자가") {
            val email = TEST_EMAIL
            val valOps = mockk<ValueOperations<String, String>>()
            every { redisTemplate.opsForValue() } returns valOps
            every { redisTemplate.delete(any<String>()) } returns true
            every { redisTemplate.expire(any(), any()) } returns true
            every { valOps.set(any(), any()) } just Runs
            every { mailSender.send(any<SimpleMailMessage>()) } just Runs

            When("이메일 인증 코드 생성을 요청하면") {
                authService.createEmailVerificationCode(email)

                Then("존재할지 모르는 이메일 인증 코드가 삭제된다.") {
                    verify(exactly = 1) { redisTemplate.delete("$EMAIL_PREFIX$email") }
                }

                Then("5자리 숫자로된 코드가 Redis에 등록된다.") {
                    verify(exactly = 1) {
                        valOps.set(
                            eq("$EMAIL_PREFIX$email"),
                            match { it.length == 5 && it.all { c -> c.isDigit() } },
                        )
                    }
                }

                Then("인증 코드의 유효시간은 5분이다.") {
                    verify(exactly = 1) {
                        redisTemplate.expire(
                            "$EMAIL_PREFIX$email",
                            Duration.ofMinutes(VERIFICATION_TTL),
                        )
                    }
                }

                Then("인증코드가 메일로 발송된다.") {
                    verify(exactly = 1) { mailSender.send(any<SimpleMailMessage>()) }
                }
            }
        }

        Given("이메일 인증 요청한 사용자가") {
            val email = TEST_EMAIL
            val code = VERIFICATION_CODE
            val valOps = mockk<ValueOperations<String, String>>()
            every { redisTemplate.opsForValue() } returns valOps
            every { redisTemplate.delete(any<String>()) } returns true
            every { valOps.get("$EMAIL_PREFIX$email") } returns code

            When("이메일 인증을 성공하면") {
                authService.verifyCode(email, code, VerificationType.EMAIL)

                Then("Redis에 저정된 이메일 인증 코드가 삭제된다.") {
                    verify(exactly = 1) { redisTemplate.delete("$EMAIL_PREFIX$email") }
                }
            }

            When("인증 시간이 만료되면") {
                every { valOps.get("$EMAIL_PREFIX$email") } returns null

                Then("만료된 코드 예외가 발생한다.") {
                    val exception =
                        shouldThrow<CommonException> {
                            authService.verifyCode(email, code, VerificationType.EMAIL)
                        }
                    exception.errorCode shouldBe ErrorCode.EXPIRED_CODE
                }
            }

            When("이메일 인증에 실패하면") {
                val wrongCode = "wrong"
                every { valOps.get("$EMAIL_PREFIX$email") } returns wrongCode

                Then("유효하지 않은 코드 예외가 발생한다.") {
                    val exception =
                        shouldThrow<CommonException> {
                            authService.verifyCode(email, code, VerificationType.EMAIL)
                        }
                    exception.errorCode shouldBe ErrorCode.INVALID_CODE
                }
            }
        }

        // 휴대폰 인증 코드 생성 요청 테스트
        Given("이메일을 인증한 사용자가") {
            val phone = TEST_PHONE
            val valOps = mockk<ValueOperations<String, String>>()
            every { redisTemplate.opsForValue() } returns valOps
            every { redisTemplate.delete(any<String>()) } returns true
            every { valOps.set(any(), any()) } just Runs
            every { redisTemplate.expire(any(), any()) } returns true

            When("휴대폰 인증 코드 생성을 요청하면") {
                authService.createPhoneVerificationCode(phone)

                Then("존재할지 모르는 휴대폰 인증 코드가 삭제된다.") {
                    verify(exactly = 1) { redisTemplate.delete("$PHONE_PREFIX$phone") }
                }

                Then("5자리 숫자로된 코드가 Redis에 등록된다.") {
                    verify(exactly = 1) {
                        valOps.set(
                            eq("$PHONE_PREFIX$phone"),
                            match { it.length == 5 && it.all { c -> c.isDigit() } },
                        )
                    }
                }
            }
        }

        // 휴대폰 인증 요청 테스트
        Given("휴대폰 인증 요청한 사용자가") {
            val phone = TEST_PHONE
            val code = VERIFICATION_CODE
            val valOps = mockk<ValueOperations<String, String>>()
            every { redisTemplate.opsForValue() } returns valOps
            every { valOps.get("$PHONE_PREFIX$phone") } returns code
            every { redisTemplate.delete(any<String>()) } returns true

            When("휴대폰 인증을 성공하면") {
                authService.verifyCode(phone, code, VerificationType.PHONE)

                Then("Redis에 저정된 휴대폰 인증 코드가 삭제된다.") {
                    verify(exactly = 1) { redisTemplate.delete("$PHONE_PREFIX$phone") }
                }
            }

            When("인증 시간이 만료되면") {
                every { valOps.get("$PHONE_PREFIX$phone") } returns null

                Then("만료된 코드 예외가 발생한다.") {
                    val exception =
                        shouldThrow<CommonException> {
                            authService.verifyCode(phone, code, VerificationType.PHONE)
                        }
                    exception.errorCode shouldBe ErrorCode.EXPIRED_CODE
                }
            }

            When("휴대폰 인증에 실패하면") {
                val wrongCode = "wrong"
                every { valOps.get("$PHONE_PREFIX$phone") } returns wrongCode

                Then("유효하지 않은 코드 예외가 발생한다.") {
                    val exception =
                        shouldThrow<CommonException> {
                            authService.verifyCode(phone, code, VerificationType.PHONE)
                        }
                    exception.errorCode shouldBe ErrorCode.INVALID_CODE
                }
            }
        }
    })