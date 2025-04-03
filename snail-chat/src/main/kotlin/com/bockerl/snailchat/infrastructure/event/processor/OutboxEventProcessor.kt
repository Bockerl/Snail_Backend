package com.bockerl.snailchat.infrastructure.event.processor

import com.bockerl.snailchat.infrastructure.outbox.entity.Outbox
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
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
    fun process(event: Outbox) {
        logger.info { "Outbox 이벤트 전송 시작: ${event.outboxId}" }

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

//        kafkaTemplate.send(event.eventType.topic, message).addCallback(
//            { result ->
//                logger.info { "Outbox 이벤트 전송 성공: ${event.outboxId}" }
//                // 전송 성공 시 추가 작업(예: 상태 업데이트)을 수행할 수 있음
//            },
//            { ex ->
//                logger.error(ex) { "Outbox 이벤트 전송 실패: ${event.outboxId}" }
//                // 전송 실패 시, DLQ 처리 (비동기적으로 처리)
//                dlqProcessor.sendToDlq(event)
//            },
//        )
    }
}