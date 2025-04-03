package com.bockerl.snailchat.infrastructure.event.publisher

import com.bockerl.snailchat.infrastructure.event.processor.DlqProcessor
import com.bockerl.snailchat.infrastructure.event.processor.OutboxEventProcessor
import com.bockerl.snailchat.infrastructure.outbox.enums.OutboxStatus
import com.bockerl.snailchat.infrastructure.outbox.service.OutboxService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class OutboxPublisher(
    private val outboxService: OutboxService,
    private val outboxEventProcessor: OutboxEventProcessor,
    private val dlqProcessor: DlqProcessor,
) {
    private val logger = KotlinLogging.logger { }
    private val maxRetry = 3

    @Scheduled(fixedDelay = 1000)
    fun publishOutbox() {
        val pendingOutbox = outboxService.findByStatus(listOf(OutboxStatus.PENDING, OutboxStatus.RETRYING))

        pendingOutbox.forEach { event ->
            try {
                // 중복 방지를 위해 진행 상태 나타내줌
                event.status = OutboxStatus.PROCESSING
                outboxService.changeStatus(event)

                outboxEventProcessor.process(event)

                // 성공상태 반영
                event.status = OutboxStatus.SUCCESS
                outboxService.changeStatus(event)
            } catch (e: Exception) {
                logger.error(e) { "Outbox 이벤트 처리 중 예외 발생 " }

                // 실패시 재시도를 위해서 카운트 증가
                event.retryCount += 1

                // 재시도 횟수를 모두 넘기면 dlq로 전송 ( Publisher 오류로 인한 )
                if (event.retryCount >= maxRetry) {
                    event.status = OutboxStatus.DEAD // DEAD 상태로 변경후 DLQ로 전송

                    dlqProcessor.sendToDlq(event)
                } else {
                    event.status = OutboxStatus.RETRYING // 재시도
                }

                outboxService.changeStatus(event)
            }
        }
    }
}