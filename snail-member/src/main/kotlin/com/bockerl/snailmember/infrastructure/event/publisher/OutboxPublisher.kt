package com.bockerl.snailmember.infrastructure.event.publisher

import com.bockerl.snailmember.infrastructure.outbox.repository.OutboxRepository
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class OutboxPublisher(
    private val outboxRepository: OutboxRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) {
    @Scheduled(fixedDelay = 5000)
    fun publishOutbox() {
        val pendingEvents = outboxRepository.findByStatus("PENDING")
        pendingEvents.forEach { event ->
            try {
                // 설명. kafka로 이벤트 발행(동기식 전송을 위해 .get() 호출하기)
                kafkaTemplate.send(event.eventType.topic, event.payload).get()
                // 설명. 발행 성공 시 이벤트 상태를 SENT로 변경
                event.status = "SENT"
                outboxRepository.save(event)
            } catch (e: Exception) {
                // 설명. 재시도 로직 ㄱ
            }
        }
    }
}