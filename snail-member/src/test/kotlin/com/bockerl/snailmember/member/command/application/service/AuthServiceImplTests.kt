@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.config.TestConfiguration
import com.bockerl.snailmember.config.TestSupport
import com.bockerl.snailmember.member.command.domain.aggregate.entity.tempMember.VerificationType
import org.junit.jupiter.api.*
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Import(TestConfiguration::class)
class AuthServiceImplTests : TestSupport() {
    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @Autowired
    private lateinit var mailSender: JavaMailSender

    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        // 모든 의존성이 주입된 후에 authService 생성
        authService = AuthServiceImpl(redisTemplate, mailSender)
    }

    companion object {
        private const val EMAIL_PREFIX = "verification:email:"
        private const val PHONE_PREFIX = "verification:phone:"
        private const val TEST_EMAIL = "test@test.com"
        private const val TEST_PHONE = "01012345678"
        private const val VERIFICATION_CODE = "12345"
        private const val VERIFICATION_TTL = 5L
    }

    @Nested
    @DisplayName("이메일 인증 관련 테스트")
    inner class EmailVerification {
        @Test
        @DisplayName("이메일 인증 코드 발급 성공 테스트")
        fun emailVerification_create_success() {
            // given
            val valOps: ValueOperations<String, String> = mock()
            whenever(redisTemplate.opsForValue()).thenReturn(valOps)
            doNothing().`when`(valOps).set(any(), any())
            whenever(redisTemplate.expire(any(), any())).thenReturn(true)
            doNothing().`when`(mailSender).send(any<SimpleMailMessage>())

            // when
            authService.createEmailVerificationCode(TEST_EMAIL)

            // then
            // 혹시 있을 키 삭제 검증
            verify(redisTemplate).delete("$EMAIL_PREFIX$TEST_EMAIL")
            // key에 5자리 숫자로 구성된 코드 등록 검증
            verify(valOps).set(
                eq("$EMAIL_PREFIX$TEST_EMAIL"),
                argThat { it.length == 5 && it.all { c -> c.isDigit() } },
            )
            // TTL 검증
            verify(redisTemplate).expire(
                eq("$EMAIL_PREFIX$TEST_EMAIL"),
                eq(Duration.ofMinutes(VERIFICATION_TTL)),
            )
            // 메일 전송 검증
            verify(mailSender).send(any<SimpleMailMessage>())
        }

        @Test
        @DisplayName("이메일 인증 요청 성공 테스트")
        fun emailVerification_success() {
            // given
            val valOps: ValueOperations<String, String> = mock()
            whenever(redisTemplate.opsForValue()).thenReturn(valOps)
            whenever(valOps.get("$EMAIL_PREFIX$TEST_EMAIL")).thenReturn(VERIFICATION_CODE)

            // when & then
            assertDoesNotThrow {
                authService.verifyCode(TEST_EMAIL, VERIFICATION_CODE, VerificationType.EMAIL)
            }
            verify(redisTemplate).delete("$EMAIL_PREFIX$TEST_EMAIL")
        }

        @Test
        @DisplayName("이메일 인증 실패 - 만료된 코드")
        fun emailVerification_failure_expire() {
            // given
            val valOps: ValueOperations<String, String> = mock()
            whenever(redisTemplate.opsForValue()).thenReturn(valOps)
            // Redis에 저장된 코드가 없다고 가정(만료)
            whenever(valOps.get("$EMAIL_PREFIX$TEST_EMAIL")).thenReturn(null)

            // when $ then
            val exception =
                assertThrows<CommonException> {
                    authService.verifyCode(TEST_EMAIL, VERIFICATION_CODE, VerificationType.EMAIL)
                }
            assertEquals(exception.errorCode, ErrorCode.EXPIRED_CODE)
        }

        @Test
        @DisplayName("이메일 인증 실패 - 잘못된 코드")
        fun emailVerification_failure_invalid() {
            // given
            val valOps: ValueOperations<String, String> = mock()
            whenever(redisTemplate.opsForValue()).thenReturn(valOps)
            // Redis에 저장된 코드와 사용자 코드가 일치하지 않음
            whenever(valOps.get("$EMAIL_PREFIX$TEST_EMAIL")).thenReturn(VERIFICATION_CODE)

            // when & then
            val exception =
                assertThrows<CommonException> {
                    authService.verifyCode(TEST_EMAIL, "wrong_code", VerificationType.EMAIL)
                }
            assertEquals(exception.errorCode, ErrorCode.INVALID_CODE)
        }
    }

    @Nested
    @DisplayName("휴대폰 인증 관련 테스트")
    inner class PhoneVerification {
        @Test
        @DisplayName("휴대폰 인증 코드 생성 성공 테스트")
        fun phoneVerification_create_success() {
            // given
            val valOps: ValueOperations<String, String> = mock()
            whenever(redisTemplate.opsForValue()).thenReturn(valOps)
            doNothing().`when`(valOps).set(any(), any())
            whenever(redisTemplate.expire(any(), any())).thenReturn(true)

            // when
            val result = authService.createPhoneVerificationCode(TEST_PHONE)

            // then
            // 생성한 인증 코드가 5자리 숫자인지 검증
            assertTrue(result.length == 5 && result.all { c -> c.isDigit() })
            // 혹시 모를 키 삭제 검증
            verify(redisTemplate).delete("$PHONE_PREFIX$TEST_PHONE")
            // Redis 등록 검증
            verify(valOps).set(
                eq("$PHONE_PREFIX$TEST_PHONE"),
                argThat { it.length == 5 && it.all { c -> c.isDigit() } },
            )
            // TTL 검증
            verify(redisTemplate).expire(
                eq("$PHONE_PREFIX$TEST_PHONE"),
                eq(Duration.ofMinutes(VERIFICATION_TTL)),
            )
        }

        @Test
        @DisplayName("휴대폰 인증 요청 성공 테스트")
        fun phoneVerification_success() {
            // given
            val valOps: ValueOperations<String, String> = mock()
            whenever(redisTemplate.opsForValue()).thenReturn(valOps)
            whenever(valOps.get("$PHONE_PREFIX$TEST_PHONE")).thenReturn(VERIFICATION_CODE)

            // when & then
            assertDoesNotThrow {
                authService.verifyCode(TEST_PHONE, VERIFICATION_CODE, VerificationType.PHONE)
            }
            verify(redisTemplate).delete("$PHONE_PREFIX$TEST_PHONE")
        }

        @Test
        @DisplayName("휴대폰 인증 실패 - 만료된 코드")
        fun phoneVerification_failure_expire() {
            // given
            val valOps: ValueOperations<String, String> = mock()
            whenever(redisTemplate.opsForValue()).thenReturn(valOps)
            // Redis에 저장된 코드가 없다고 가정(만료)
            whenever(valOps.get("$PHONE_PREFIX$TEST_PHONE")).thenReturn(null)

            // when & then
            val exception =
                assertThrows<CommonException> {
                    authService.verifyCode(TEST_PHONE, "$PHONE_PREFIX$TEST_PHONE", VerificationType.PHONE)
                }
            assertEquals(exception.errorCode, ErrorCode.EXPIRED_CODE)
        }

        @Test
        @DisplayName("휴대폰 인증 실패 - 잘못된 코드")
        fun phoneVerification_failure_invalid() {
            val valOps: ValueOperations<String, String> = mock()
            whenever(redisTemplate.opsForValue()).thenReturn(valOps)
            whenever(valOps.get("$PHONE_PREFIX$TEST_PHONE")).thenReturn(VERIFICATION_CODE)

            val exception =
                assertThrows<CommonException> {
                    authService.verifyCode(TEST_PHONE, "wrong_code", VerificationType.PHONE)
                }
            assertEquals(exception.errorCode, ErrorCode.INVALID_CODE)
        }
    }
}