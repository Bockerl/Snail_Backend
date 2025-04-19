package com.bockerl.snailchat.infrastructure.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Configuration

@EnableCaching
@Configuration
class RedisConfig(
    @Value("\${REDIS_MASTER}") private val redisMaster: String,
    @Value("\${REDIS_PORT1}") private val redisPort1: String,
    @Value("\${REDIS_PORT2}") private val redisPort2: String,
    @Value("\${REDIS_PORT3}") private val redisPort3: String,
    @Value("\${DB_PASSWORD}") private val redisPassword: String,
    private val objectMapper: ObjectMapper,
) {
    private val logger = KotlinLogging.logger { }
}