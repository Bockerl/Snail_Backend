package com.bockerl.snailchat.infrastructure.event.processor

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDTO
import com.bockerl.snailchat.infrastructure.outbox.entity.Outbox
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.TransientDataAccessException
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class OutboxEventProcessor(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val objectMapper: ObjectMapper,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Outbox 이벤트를 Kafka로 동기식 전송합니다.
     * 재시도 정책을 적용하여 일시적 오류 발생 시 최대 3회 재시도를 수행합니다.
     */
    @Retryable(
        value = [TransientDataAccessException::class],
        maxAttempts = 3,
        // delay를 다음번에 delay * multiplier의 대기시간, random은 이 시간 아래의 무작위 값
        backoff = Backoff(delay = 1000, multiplier = 2.0, random = true),
    )
    fun process(event: Outbox) {
        logger.info { "Outbox 이벤트 전송 시작: ${event.outboxId}" }

//        val headers =
//            MessageHeaders(
//                mapOf(
//                    "eventId" to event.eventId,
//                    "idempotencyKey" to event.idempotencyKey,
//                ),
//            )
//
//        val message =
//            MessageBuilder
//                .withPayload(event.payload)
//                .copyHeaders(headers)
//                .build()

        val payloadJson = event.payload
        val commandChatMessageRequestDTO = objectMapper.readValue(payloadJson, CommandChatMessageRequestDTO::class.java)

        try {
            val result = kafkaTemplate.send(event.eventType.topic, event.aggregateId, commandChatMessageRequestDTO).get()
            logger.info {
                "Kafka 전송 성공 - outboxId=${event.outboxId}, partition=${result.recordMetadata.partition()}, offset=${result.recordMetadata.offset()}"
            }
        } catch (e: Exception) {
            logger.info { "Outbox 이벤트 전송 실패: ${event.outboxId}" }
            throw e
        }
    }

    @Recover
    fun recover(
        e: Exception,
        event: Outbox,
    ) {
        logger.error(e) { "Kafka 전송 3회 실패 → Recover 진입: ${event.outboxId}" }
        throw e // 혹은 여기서 직접 DLQ 처리할 수도 있음 (현재 구조상 Publisher에서 처리하므로 throw 유지)
    }
}