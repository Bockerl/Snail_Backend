package com.bockerl.snailchat.infrastructure.outbox.service

import com.bockerl.snailchat.infrastructure.outbox.dto.OutboxDTO
import com.bockerl.snailchat.infrastructure.outbox.entity.Outbox
import com.bockerl.snailchat.infrastructure.outbox.enums.OutboxStatus

interface OutboxService {
    fun createOutbox(outboxDTO: OutboxDTO)

    fun existsByIdempotencyKey(idempotencyKey: String): Boolean

    fun findByStatus(status: List<OutboxStatus>): List<Outbox>

    fun changeStatus(event: Outbox)

    fun changeStatusAndRetryCount(event: Outbox)
}