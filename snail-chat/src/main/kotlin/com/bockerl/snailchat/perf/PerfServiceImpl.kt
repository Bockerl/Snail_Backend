package com.bockerl.snailchat.perf

import com.bockerl.snailchat.infrastructure.outbox.service.OutboxService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class PerfServiceImpl(
    private val outboxService: OutboxService,
) : PerfService {
    private val logger = KotlinLogging.logger {}

    override fun getIdempotencyKey(key: String): Boolean {
        if (outboxService.existsByIdempotencyKey(key)) {
            logger.info { "중복 요청: 이미 처리된 idempotencyKey = $key" }
            return false
        }

        return true
    }

    override fun getIdempotencyKeyWithRedis(key: String): Boolean = true
}