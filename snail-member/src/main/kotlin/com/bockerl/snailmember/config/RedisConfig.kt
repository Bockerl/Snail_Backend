package com.bockerl.snailmember.config

import com.bockerl.snailmember.member.command.domain.aggregate.entity.tempMember.TempMember
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisSentinelConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig(
    @Value("\${REDIS_MASTER}") private val redisMaster: String,
    @Value("\${REDIS_PORT1}") private val redisPort1: String,
    @Value("\${REDIS_PORT2}") private val redisPort2: String,
    @Value("\${REDIS_PORT3}") private val redisPort3: String,
    private val objectMapper: ObjectMapper,
) {
    private val logger = KotlinLogging.logger {}

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val sentinelConfig =
            RedisSentinelConfiguration().apply {
                master(redisMaster)
                logger.info { "Redis Master: $redisMaster" }
                // 각 노드를 host와 port로 분리해서 설정
                val (host1, port1) = parseHostPort(redisPort1)
                logger.info { "Sentinel 1: $host1:$port1" }
                sentinel(host1, port1)
                val (host2, port2) = parseHostPort(redisPort2)
                logger.info { "Sentinel 2: $host2:$port2" }
                sentinel(host2, port2)
                val (host3, port3) = parseHostPort(redisPort3)
                logger.info { "Sentinel 3: $host3:$port3" }
                sentinel(host3, port3)
            }
        val factory = LettuceConnectionFactory(sentinelConfig)
        factory.afterPropertiesSet() // 명시적 초기화
        return factory

//        val redisConfig = RedisStandaloneConfiguration(host, port)
//        return LettuceConnectionFactory(redisConfig)
    }

    // host:port 문자열을 분리하는 헬퍼 함수
    private fun parseHostPort(address: String): Pair<String, Int> {
        val parts = address.split(":")
        require(parts.size == 2) { "Invalid sentinel node address: $address" }
        return parts[0] to parts[1].toInt()
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
}
