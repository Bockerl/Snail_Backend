package com.bockerl.snailmember.config

import com.bockerl.snailmember.member.command.application.service.AuthService
import com.bockerl.snailmember.member.command.application.service.AuthServiceImpl
import org.mockito.Mockito.mock
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.mail.javamail.JavaMailSender

@TestConfiguration
class AuthTestConfiguration {
    // SpringBootTest는 실제 Application처럼 동작하기 위해 전체 Application Context를 로드합니다.
    // 이 과정에서 컴포넌트 스캔으로 실제 서비스에 사용될 bean과 테스트에 사용될 bean의 충돌이 발생해서 @Primary를 달았습니다.
    @Primary
    @Bean
    fun mockRedisTemplate(): RedisTemplate<String, String> = mock()

    @Primary
    @Bean
    fun mockMailSender(): JavaMailSender = mock()

    @Primary
    @Bean
    fun mockAuthService(
        redisTemplate: RedisTemplate<String, String>,
        mailSender: JavaMailSender,
    ): AuthService = AuthServiceImpl(redisTemplate, mailSender)
}