package com.bockerl.snailchat.infrastructure.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.oshai.kotlinlogging.KotlinLogging
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
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@EnableCaching
@Configuration
class RedisConfig(
    @Value("\${REDIS_MASTER}") private val redisMaster: String,
    @Value("\${REDIS_PORT1}") private val redisPort1: String,
    @Value("\${REDIS_PORT2}") private val redisPort2: String,
    @Value("\${REDIS_PORT3}") private val redisPort3: String,
    @Value("\${DB_PASSWORD}") private val redisPassword: String,
) {
    private val logger = KotlinLogging.logger { }

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val sentinelConfig =
            RedisSentinelConfiguration().apply {
                master(redisMaster)
                logger.info { "redisMaster: " + redisMaster }
                // localhost:port번호를 분리해서 대입
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

    // host:port 분리 ( redisPort1은 Localhost:포트번호 )
    private fun parseHostPort(address: String): Pair<String, Int> {
        val parts = address.split(":")
        require(parts.size == 2) { "sentinel 주소가 잘못되었습니다: $address" }
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
                    // Kotlin 클래스 처리 설정
                    registerModule(KotlinModule.Builder().build())
                    // 타입 정보 포함
                    activateDefaultTyping(
                        LaissezFaireSubTypeValidator.instance,
                        ObjectMapper.DefaultTyping.NON_FINAL,
                        JsonTypeInfo.As.WRAPPER_ARRAY,
                    )
                    // Java 8 날짜/시간 모듈
                    registerModule(JavaTimeModule())
                    // 날짜/시간을 ISO-8601 형식 문자열로 직렬화
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

            // 직렬화 설정 적용 된 후, 템플릿 초기화 : IllegalStateException 방지
            afterPropertiesSet()
        }

    // 캐시 매니져
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