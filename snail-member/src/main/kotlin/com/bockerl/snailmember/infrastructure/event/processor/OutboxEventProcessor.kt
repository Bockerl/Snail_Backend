package com.bockerl.snailmember.infrastructure.event.processor

import com.bockerl.snailmember.infrastructure.config.TransactionalConfig
import com.bockerl.snailmember.infrastructure.outbox.entity.Outbox
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.TransientDataAccessException
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
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
        // 설명. delay를 다음번에 delay * multiplier의 대기시간, random은 이 시간 아래의 무작위 값
        backoff = Backoff(delay = 1000, multiplier = 2.0, random = true),
    )
    fun process(event: Outbox) {
        logger.info { "Outbox 이벤트 전송 시작: ${event.outboxId}" }

        TransactionalConfig.run {
            val headers =
                MessageHeaders(
                    mapOf(
                        "eventId" to event.eventId,
                        "idempotencyKey" to event.idempotencyKey,
                    ),
                )

            val message =
                MessageBuilder
                    .withPayload(event.payload)
                    .copyHeaders(headers)
                    .build()

            kafkaTemplate.send(event.eventType.topic, message).get()
        }

        // 동기식 전송: .get() 호출로 전송 결과를 기다림
//        kafkaTemplate.send(event.eventType.topic, event.payload).get()
        logger.info { "Outbox 이벤트 전송 성공: ${event.outboxId}" }
    }

    /**
     * 재시도 실패 시 호출되는 회복 메서드.
     * DLQ로 이벤트를 전송하고 로깅합니다.
     */
    @Recover
    fun recover(
        ex: Exception,
        event: Outbox,
    ) {
        logger.error(ex) { "Outbox 이벤트 전송 실패. DLQ로 전송: ${event.outboxId}" }
        dlqProcessor.sendToDlq(event)
    }
}