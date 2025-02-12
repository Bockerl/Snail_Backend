package com.bockerl.snailmember.config

import com.bockerl.snailmember.member.command.domain.aggregate.entity.tempMember.TempMember
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig(
    @Value("\${REDIS_HOST}") private val host: String,
    @Value("\${REDIS_PORT}") private val port: Int,
    private val objectMapper: ObjectMapper,
) {
    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val redisConfig = RedisStandaloneConfiguration(host, port)
        return LettuceConnectionFactory(redisConfig)
    }

    @Bean
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, String> =
        RedisTemplate<String, String>().apply {
            this.connectionFactory = redisConnectionFactory
            this.keySerializer = StringRedisSerializer()
            this.valueSerializer = StringRedisSerializer()
        }

    @Bean
    fun tempMemberRedisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, TempMember> =
        RedisTemplate<String, TempMember>().apply {
            // Redis 연결 설정
            this.connectionFactory = redisConnectionFactory
            // 키는 문자열로 저장
            this.keySerializer = StringRedisSerializer()
            // GenericJackson2JsonRedisSerializer 사용
            val jsonRedisSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
            // 값 직렬화 설정
            this.valueSerializer = jsonRedisSerializer
            // Hash 작업을 위한 직렬화 설정
            this.hashKeySerializer = StringRedisSerializer()
            this.hashValueSerializer = jsonRedisSerializer
        }
}