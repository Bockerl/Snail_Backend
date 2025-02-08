@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.config.AuthTestConfiguration
import com.bockerl.snailmember.config.TestSupport
import com.bockerl.snailmember.member.command.domain.aggregate.entity.tempMember.VerificationType
import org.junit.jupiter.api.*
import org.mockito.Mockito.*
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import java.time.Duration
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Import(AuthTestConfiguration::class)
class AuthServiceImplTests(
    private val authService: AuthService,
    private val redisTemplate: RedisTemplate<String, String>,
    private val mailSender: JavaMailSender,
) : TestSupport() {
    companion object {
        private const val TEST_EMAIL = "test@test.com"
        private const val TEST_PHONE = "01012345678"
        private const val VERIFICATION_TTL = 5L
        private const val VERIFICATION_CODE = "12345"
    }

    @BeforeEach
    fun setup() {
        val valOps: ValueOperations<String, String> = mock()
        whenever(redisTemplate.opsForValue()).thenReturn(valOps)
    }

    @Nested
    @DisplayName("이메일 인증 관련 테스트")
    inner class EmailVerification {
        @Test
        @DisplayName("이메일 인증 코드 발급 성공 테스트")
        fun emailVerification_create_success() {
            // given
            val email = TEST_EMAIL

            // when
            authService.createEmailVerificationCode(email)

            // then
            val inOrder = inOrder(redisTemplate, redisTemplate.opsForValue(), mailSender)

            inOrder.verify(redisTemplate).delete("verification:email:$email")
            inOrder.verify(redisTemplate.opsForValue()).set(
                eq("verification:email:$email"),
                argThat { it.length == 5 },
            )
            inOrder.verify(redisTemplate).expire(
                eq("verification:email:$email"),
                eq(Duration.ofMinutes(VERIFICATION_TTL)),
            )
            inOrder.verify(mailSender).send(any<SimpleMailMessage>())
        }

        @Test
        @DisplayName("이메일 인증 코드 재발급 테스트")
        fun emailVerification_recreate_success() {
            // given
            val email = TEST_EMAIL
            whenever(redisTemplate.delete("verification:email:$email")).thenReturn(true)

            // when
            authService.createEmailVerificationCode(email)

            // then
            val inOrder = inOrder(redisTemplate, redisTemplate.opsForValue(), mailSender)

            inOrder.verify(redisTemplate).delete("verification:email:$email")
            inOrder.verify(redisTemplate.opsForValue()).set(
                eq("verification:email:$email"),
                argThat { it.length == 5 },
            )
            inOrder.verify(redisTemplate).expire(
                eq("verification:email:$email"),
                eq(Duration.ofMinutes(VERIFICATION_TTL)),
            )
            inOrder.verify(mailSender).send(any<SimpleMailMessage>())
        }

        @Test
        @DisplayName("이메일 인증 요청 성공 테스트")
        fun emailVerification_success() {
            // given
            val email = TEST_EMAIL
            whenever(redisTemplate.opsForValue().get("verification:email:$email"))
                .thenReturn(VERIFICATION_CODE)

            // when & then
            assertDoesNotThrow {
                authService.verifyCode(email, VERIFICATION_CODE, VerificationType.EMAIL)
            }
            verify(redisTemplate).delete("verification:email:$email")
        }
    }

    @Nested
    @DisplayName("휴대폰 인증 관련 테스트")
    inner class PhoneVerification {
        @Test
        @DisplayName("휴대폰 인증 코드 생성 성공 테스트")
        fun phoneVerification_create_success() {
            // given
            val phoneNumber = TEST_PHONE

            // when
            val code = authService.createPhoneVerificationCode(phoneNumber)

            // then
            val inOrder = inOrder(redisTemplate, redisTemplate.opsForValue())

            assertNotNull(code)
            assertTrue { code.length == 5 && code.all { it.isDigit() } }
            inOrder.verify(redisTemplate).delete("verification:phone:$phoneNumber")
            inOrder.verify(redisTemplate.opsForValue()).set(
                eq("verification:phone:$phoneNumber"),
                eq(code),
            )
            inOrder.verify(redisTemplate).expire(
                eq("verification:phone:$phoneNumber"),
                eq(Duration.ofMinutes(VERIFICATION_TTL)),
            )
        }

        @Test
        @DisplayName("휴대폰 인증 요청 성공 테스트")
        fun phoneVerification_success() {
            // given
            val phoneNumber = TEST_PHONE
            val code = VERIFICATION_CODE // 상수 사용

            whenever(redisTemplate.opsForValue().get("verification:phone:$phoneNumber"))
                .thenReturn(code)

            // when & then
            assertDoesNotThrow {
                authService.verifyCode(phoneNumber, code, VerificationType.PHONE)
            }
            verify(redisTemplate).delete("verification:phone:$phoneNumber")
        }
    }
}