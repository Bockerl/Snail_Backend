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
}