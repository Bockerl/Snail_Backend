package com.bockerl.snailmember.infrastructure.outbox.repository

import com.bockerl.snailmember.infrastructure.outbox.entity.Outbox
import org.springframework.data.jpa.repository.JpaRepository

interface OutboxRepository : JpaRepository<Outbox, Long> {
    fun findByStatus(status: String): List<Outbox>
}