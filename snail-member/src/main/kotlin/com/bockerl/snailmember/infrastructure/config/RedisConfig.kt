package com.bockerl.snailmember.infrastructure.config

import com.bockerl.snailmember.member.command.domain.aggregate.entity.tempMember.TempMember
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisSentinelConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
// 설명. 캐시 기능 활성화 어노테이션
@EnableCaching
class RedisConfig(
//    @Value("\${REDIS_HOST}") private val host: String,
//    @Value("\${REDIS_PORT}") private val port: Int,
    @Value("\${REDIS_MASTER}") private val redisMaster: String,
    @Value("\${REDIS_PORT1}") private val redisPort1: String,
    @Value("\${REDIS_PORT2}") private val redisPort2: String,
    @Value("\${REDIS_PORT3}") private val redisPort3: String,
    @Value("\${DB_PASSWORD}") private val redisPassword: String,
    private val objectMapper: ObjectMapper,
) {
    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val sentinelConfig =
            RedisSentinelConfiguration().apply {
                master(redisMaster)
                // 각 노드를 host와 port로 분리해서 설정
                val (host1, port1) = parseHostPort(redisPort1)
                sentinel(host1, port1)
                val (host2, port2) = parseHostPort(redisPort2)
                sentinel(host2, port2)
                val (host3, port3) = parseHostPort(redisPort3)
                sentinel(host3, port3)
                setPassword(redisPassword)
            }
        return LettuceConnectionFactory(sentinelConfig)
    }

    // host:port 문자열을 분리하는 헬퍼 함수
    private fun parseHostPort(address: String): Pair<String, Int> {
        val parts = address.split(":")
        require(parts.size == 2) { "Invalid sentinel node address: $address" }
        return parts[0] to parts[1].toInt()
    }

    @Bean
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> =
        RedisTemplate<String, Any>().apply {
            this.connectionFactory = redisConnectionFactory
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

    @Bean
    fun tempMemberRedisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, TempMember> =
        RedisTemplate<String, TempMember>().apply {
            // Redis 연결 설정
            this.connectionFactory = redisConnectionFactory
            // 키는 문자열로 저장
            this.keySerializer = StringRedisSerializer()
            // Jackson2JsonRedisSerializer 사용
            val jsonRedisSerializer =
                Jackson2JsonRedisSerializer(objectMapper, TempMember::class.java)
            // 값 직렬화 설정
            this.valueSerializer = jsonRedisSerializer
            // Hash 작업을 위한 직렬화 설정
            this.hashKeySerializer = StringRedisSerializer()
            this.hashValueSerializer = jsonRedisSerializer
            afterPropertiesSet()
        }

    // 설명. 캐시 매니저
    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): RedisCacheManager {
        val configuration =
            RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5)) // 캐시 TTL 5분 설정
        return RedisCacheManager
            .builder(redisConnectionFactory)
            .cacheDefaults(configuration)
            .build()
    }
}