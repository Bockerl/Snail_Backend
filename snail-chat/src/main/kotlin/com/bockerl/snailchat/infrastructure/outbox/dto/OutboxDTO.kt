package com.bockerl.snailchat.infrastructure.outbox.dto

import com.bockerl.snailchat.infrastructure.outbox.enums.EventType

class OutboxDTO(
    val aggregateId: String,
    val eventType: EventType,
    val payload: String,
    val idempotencyKey: String,
)