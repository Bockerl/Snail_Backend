package com.bockerl.snailchat.infrastructure.event.processor

import com.bockerl.snailchat.infrastructure.outbox.entity.Outbox
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.TransientDataAccessException
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class OutboxEventProcessor(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val dlqProcessor: DlqProcessor,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Outbox 이벤트를 Kafka로 전송합니다.
     * 재시도 정책을 적용하여 일시적 오류 발생 시 최대 3회 재시도를 수행합니다.
     */
    @Retryable(
        value = [TransientDataAccessException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 1000, multiplier = 2.0, random = true), // delay 시간이 점차 증가
    )
    fun process(event: Outbox) {
        logger.info { "Outbox 이벤트 전송 시작: ${event.outboxId}" }

        // 트랜잭션 적용 되기 전
    }
}