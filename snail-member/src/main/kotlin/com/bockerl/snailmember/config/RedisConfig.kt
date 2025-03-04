package com.bockerl.snailmember.config

import com.bockerl.snailmember.member.command.domain.aggregate.entity.tempMember.TempMember
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
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
            // ObjectMapper 설정
            val objectMapper =
                ObjectMapper().apply {
                    // Kotlin 클래스 처리를 위한 설정
                    registerModule(KotlinModule.Builder().build())
                    // 타입 정보를 포함하도록 설정
                    activateDefaultTyping(
                        BasicPolymorphicTypeValidator
                            .builder()
                            .allowIfBaseType(Any::class.java)
                            .build(),
                        ObjectMapper.DefaultTyping.EVERYTHING,
                    )
                    // Java 8 날짜/시간 모듈 추가
                    registerModule(JavaTimeModule())
                    // 날짜/시간을 timestamp가 아닌 ISO-8601 형식의 문자열로 직렬화
                    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    // null 필드 처리
                    setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    // 알 수 없는 프로퍼티 무시
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
            // GenericJackson2JsonRedisSerializer 사용
            val jsonRedisSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
            // 값 직렬화 설정
            this.valueSerializer = jsonRedisSerializer
            // Hash 작업을 위한 직렬화 설정
            this.hashKeySerializer = StringRedisSerializer()
            this.hashValueSerializer = jsonRedisSerializer
        }
}