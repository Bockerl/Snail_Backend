package com.bockerl.snailchat.infrastructure.event.processor

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class DlqProcessor(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) {
    private val logger = KotlinLogging.logger {}

    fun sendToDlq(event: Any) {
        val dlqTopic = "chat.outbox.producer.dlq"

        try {
            kafkaTemplate.send(dlqTopic, event).get() // get()을 통해 전송 실패 경우만 반환
            logger.info { "DLQ 전송 성공: $event" }
        } catch (e: Exception) {
            logger.error(e) { "DLQ 전송 실패: $event" }
        }
    }
}