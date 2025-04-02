package com.bockerl.snailchat.infrastructure.outbox.repository

import com.bockerl.snailchat.infrastructure.outbox.entity.Outbox
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface OutboxRepository : MongoRepository<Outbox, ObjectId> {
    fun existsByidempotencyKey(idempotencyKey: String): Boolean
}