package com.bockerl.snailmember.infrastructure.event.publisher

import com.bockerl.snailmember.infrastructure.config.TransactionalConfig
import com.bockerl.snailmember.infrastructure.event.processor.OutboxEventProcessor
import com.bockerl.snailmember.infrastructure.outbox.repository.OutboxRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class OutboxPublisher(
    private val outboxRepository: OutboxRepository,
    private val outboxEventProcessor: OutboxEventProcessor,
) {
    private val logger = KotlinLogging.logger {}

    @Scheduled(fixedDelay = 5000)
    fun publishOutbox() {
        TransactionalConfig.run {
            val pendingEvents = outboxRepository.findByStatus("PENDING")
            pendingEvents.forEach { event ->
                try {
                    // 공통 processor를 호출하여 재시도 및 DLQ 처리가 적용된 전송 수행
                    outboxEventProcessor.process(event)
                    // 설명. 발행 성공 시 이벤트 상태를 SENT로 변경
                    event.status = "SENT"
                    outboxRepository.save(event)
                } catch (e: Exception) {
                    // 여기서는 로깅 및 추가 모니터링/알림 처리 가능
                    logger.error(e) { "Outbox 이벤트 처리 중 예외 발생: ${event.outboxId}" }
                }
            }
        }
    }
}