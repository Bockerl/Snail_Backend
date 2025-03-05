package com.bockerl.snailmember.infrastructure.outbox.dto

import com.bockerl.snailmember.infrastructure.outbox.enums.EventType

class OutboxDTO(
    val aggregateId: String,
    val eventType: EventType,
    val payload: String,
)