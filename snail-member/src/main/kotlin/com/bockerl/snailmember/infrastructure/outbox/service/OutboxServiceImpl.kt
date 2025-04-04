package com.bockerl.snailmember.infrastructure.outbox.service

import com.bockerl.snailmember.infrastructure.outbox.dto.OutboxDTO
import com.bockerl.snailmember.infrastructure.outbox.entity.Outbox
import com.bockerl.snailmember.infrastructure.outbox.repository.OutboxRepository
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class OutboxServiceImpl(
    private val outboxRepository: OutboxRepository,
    private val entityManager: EntityManager,
) : OutboxService {
    @Transactional
    override fun createOutbox(outBoxDTO: OutboxDTO) {
        val outbox =
            Outbox(
                aggregateId = outBoxDTO.aggregateId,
                eventType = outBoxDTO.eventType,
                payload = outBoxDTO.payload,
                idempotencyKey = outBoxDTO.idempotencyKey,
            )
        if (outbox.eventId == null) {
            val nextVal =
                (
                    entityManager
                        .createNativeQuery("SELECT nextval('eve')")
                        .singleResult as Number
                ).toLong()
            outbox.eventId = nextVal
        }
        outboxRepository.save(outbox)
    }

    @Transactional
    override fun createOutboxes(outboxDTOs: List<OutboxDTO>) {
        val outboxes =
            outboxDTOs.map { outboxDTO ->
                val outbox =
                    Outbox(
                        aggregateId = outboxDTO.aggregateId,
                        eventType = outboxDTO.eventType,
                        payload = outboxDTO.payload,
                        // eventId로만 멱등성 처리하기
                        idempotencyKey = outboxDTO.idempotencyKey,
                    )
                if (outbox.eventId == null) {
                    val nextVal =
                        (
                            entityManager
                                .createNativeQuery("SELECT nextval('eve')")
                                .singleResult as Number
                        ).toLong()
                    outbox.eventId = nextVal
                }
                outbox
            }

        outboxRepository.saveAll(outboxes)
    }

    fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()
}