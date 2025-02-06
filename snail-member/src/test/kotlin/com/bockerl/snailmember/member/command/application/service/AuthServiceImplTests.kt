@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.config.AuthTestConfiguration
import com.bockerl.snailmember.config.TestSupport
import com.bockerl.snailmember.member.command.domain.vo.request.EmailRequestVO
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.eq
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import java.time.Duration
import java.time.LocalDate

@Import(AuthTestConfiguration::class)
class AuthServiceImplTests(
    private val authService: AuthService,
    private val redisTemplate: RedisTemplate<String, String>,
    private val mailSender: JavaMailSender,
) : TestSupport() {
    @Test
    @DisplayName("이메일 인증 코드 발급 성공 테스트")
    fun emailVerification_success() {
        // given
        val email = "test@test.com"
        val requestVO =
            EmailRequestVO(
                memberNickName = "test",
                memberEmail = email,
                memberBirth = LocalDate.now(),
            )

        // when
        authService.createEmailVerificationCode(requestVO)

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
}