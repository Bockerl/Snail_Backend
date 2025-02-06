package com.bockerl.snailmember.config

import com.bockerl.snailmember.member.command.application.service.AuthService
import com.bockerl.snailmember.member.command.application.service.AuthServiceImpl
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.mail.javamail.JavaMailSender

@TestConfiguration
class AuthTestConfiguration {
    // SpringBootTest는 실제 Application처럼 동작하기 위해 전체 Application Context를 로드합니다.
    // 이 과정에서 컴포넌트 스캔으로 실제 서비스에 사용될 bean과 테스트에 사용될 bean의 충돌이 발생해서 @Primary를 달았습니다.
    @Primary
    @Bean
    fun mockRedisTemplate(): RedisTemplate<String, String> {
        // redisTemplate mock 객체 생성 및 설정
        val redisTemplate: RedisTemplate<String, String> = mock()
        val valueOps: ValueOperations<String, String> = mock()

        // opsForValue 요청 시 mock 반환하도록 설정
        whenever(redisTemplate.opsForValue()).thenReturn(valueOps)

        return redisTemplate
    }

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