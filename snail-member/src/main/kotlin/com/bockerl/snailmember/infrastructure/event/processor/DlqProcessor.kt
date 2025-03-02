package com.bockerl.snailmember.infrastructure.event.processor

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class DlqProcessor(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) {
    private val logger = KotlinLogging.logger {}

    fun sendToDlq(event: Any) {
        // DLQ 토픽을 고정하거나, event의 타입에 따라 동적으로 선택할 수 있음.
        val dlqTopic = "dlq"
        try {
            kafkaTemplate.send(dlqTopic, event).get()
            logger.info { "DLQ 전송 성공: $event" }
        } catch (e: Exception) {
            logger.error(e) { "DLQ 전송 실패: $event" }
            // 추가적인 모니터링이나 alerting 처리 가능
        }
    }
}