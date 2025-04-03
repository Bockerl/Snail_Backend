package com.bockerl.snailchat.infrastructure.outbox.service

import com.bockerl.snailchat.infrastructure.outbox.dto.OutboxDTO
import com.bockerl.snailchat.infrastructure.outbox.entity.Outbox
import com.bockerl.snailchat.infrastructure.outbox.enums.OutboxStatus
import com.bockerl.snailchat.infrastructure.outbox.repository.OutboxRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class OutboxServiceImpl(
    private val outboxRepository: OutboxRepository,
) : OutboxService {
    private val logger = KotlinLogging.logger { }

    //    @Transactional
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

    //    @Transactional
    override fun existsByIdempotencyKey(idempotencyKey: String): Boolean = outboxRepository.existsByidempotencyKey(idempotencyKey)

    override fun findByStatus(status: List<OutboxStatus>): List<Outbox> = outboxRepository.findByStatus(OutboxStatus.PENDING)

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
}