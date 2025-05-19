package com.bockerl.snailchat.infrastructure.outbox.service

import com.bockerl.snailchat.infrastructure.outbox.dto.OutboxDTO
import com.bockerl.snailchat.infrastructure.outbox.entity.Outbox
import com.bockerl.snailchat.infrastructure.outbox.enums.OutboxStatus
import com.bockerl.snailchat.infrastructure.outbox.repository.OutboxRepository
import org.springframework.stereotype.Service

@Service
class OutboxServiceImpl(
    private val outboxRepository: OutboxRepository,
) : OutboxService {
    override fun createOutbox(outboxDTO: OutboxDTO) {
        val outbox =
            Outbox(
                aggregateId = outboxDTO.aggregateId,
                eventType = outboxDTO.eventType,
                payload = outboxDTO.payload,
                idempotencyKey = outboxDTO.idempotencyKey,
                status = OutboxStatus.PENDING,
            )
        outboxRepository.save(outbox)
    }

    override fun existsByIdempotencyKey(idempotencyKey: String): Boolean = outboxRepository.existsByIdempotencyKey(idempotencyKey)

    override fun findByStatus(status: List<OutboxStatus>): List<Outbox> = outboxRepository.findByStatusIn(status)

    override fun changeStatus(event: Outbox) {
        val outbox = outboxRepository.findById(event.outboxId)

        if (outbox.isPresent) {
            val updateOutbox = outbox.get()
            updateOutbox.status = event.status
            outboxRepository.save(updateOutbox)
        } else {
            println("해당 event의 Outbox가 존재하지 않습니다.")
        }
    }

    override fun changeStatusAndRetryCount(event: Outbox) {
        val outbox = outboxRepository.findById(event.outboxId)

        if (outbox.isPresent) {
            val updateOutbox = outbox.get()
            // 상태 초기화 (RETRYING) 하고 다시 처리 시도
            updateOutbox.status = OutboxStatus.RETRYING
            updateOutbox.retryCount = 0
            outboxRepository.save(updateOutbox)
        } else {
            println("해당 event의 Outbox가 존재하지 않습니다.")
        }
    }
}