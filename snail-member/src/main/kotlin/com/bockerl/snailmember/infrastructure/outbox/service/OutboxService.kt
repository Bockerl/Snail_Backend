package com.bockerl.snailmember.infrastructure.outbox.service

import com.bockerl.snailmember.infrastructure.outbox.dto.OutboxDTO

interface OutboxService {
    fun createOutbox(outBoxDTO: OutboxDTO)
}