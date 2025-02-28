package com.bockerl.snailmember.infrastructure.outbox.listener

import com.bockerl.snailmember.infrastructure.outbox.entity.Outbox
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.PrePersist
import org.springframework.stereotype.Component

@Component
class OutboxListener {
    @PersistenceContext
    lateinit var entityManager: EntityManager

    @PrePersist
    fun assignEventId(outbox: Outbox) {
        if (outbox.eventId == null) {
            val nextVal =
                (
                    entityManager
                        .createNativeQuery("SELECT nextval('event_id')")
                        .singleResult as Number
                ).toLong()
            outbox.eventId = nextVal
        }
    }
}