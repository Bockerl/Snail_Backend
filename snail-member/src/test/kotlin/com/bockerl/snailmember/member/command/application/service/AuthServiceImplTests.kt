@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.config.AuthTestConfiguration
import com.bockerl.snailmember.config.TestSupport
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito.*
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.RedisTemplate
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
    @Nested
    @DisplayName("이메일 인증 관련 테스트")
    inner class EmailVerification {
        @Test
        @DisplayName("이메일 인증 코드 발급 성공 테스트")
        fun emailVerification_create_success() {
            // given
            val email = "test@test.com"

            // when
            authService.createEmailVerificationCode(email)

            // then
            verify(redisTemplate.opsForValue()).set(
                argThat { it.startsWith("verification:email:") },
                argThat { it.length == 5 },
            )
            verify(redisTemplate).expire(
                argThat { it.startsWith("verification:email:") },
                eq(Duration.ofMinutes(5)),
            )
            verify(mailSender).send(any<SimpleMailMessage>())
        }

        @Test
        @DisplayName("이메일 인증 코드 재발급 테스트")
        fun emailVerification_recreate_success() {
            // given
            val email = "test@test.com"

            whenever(redisTemplate.delete("verification:email:$email")).thenReturn(true)

            // when
            authService.createEmailVerificationCode(email)

            // then
            verify(redisTemplate).delete("verification:email:$email")
            verify(redisTemplate.opsForValue()).set(
                argThat { it.startsWith("verification:email:") },
                argThat { it.length == 5 },
            )
            verify(redisTemplate).expire(
                argThat { it.startsWith("verification:email:") },
                eq(Duration.ofMinutes(5)),
            )
            verify(mailSender).send(any<SimpleMailMessage>())
        }

        @Test
        @DisplayName("이메일 인증 요청 성공 테스트")
        fun emailVerification_success() {
            // given
            val email = "test@test.com"
            val code = "12345"

            whenever(redisTemplate.opsForValue().get("verification:email:$email")).thenReturn(code)

            // when & then
            assertDoesNotThrow {
                authService.verifyEmailCode(email, code)
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
            val phoneNumber = "01012345678"

            // when
            val code = authService.createPhoneVerificationCode(phoneNumber)

            // then
            assertNotNull(code)
            assertTrue { code.length == 5 && code.all { it.isDigit() } }
            verify(redisTemplate.opsForValue()).set(
                argThat { it.startsWith("verification:phone:$phoneNumber") },
                eq(code),
            )
            verify(redisTemplate).expire(
                argThat { it.startsWith("verification:phone:$phoneNumber") },
                eq(Duration.ofMinutes(5)),
            )
        }
    }
}