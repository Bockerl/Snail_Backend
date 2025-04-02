package com.bockerl.snailchat.infrastructure.outbox.service

import com.bockerl.snailchat.infrastructure.outbox.dto.OutboxDTO

interface OutboxService {
    fun createOutbox(outboxDTO: OutboxDTO)

    fun existsByIdempotencyKey(idempotencyKey: String): Boolean
}