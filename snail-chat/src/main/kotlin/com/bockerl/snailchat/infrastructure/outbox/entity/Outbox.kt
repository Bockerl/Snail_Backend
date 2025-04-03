@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailchat.infrastructure.outbox.entity

import com.bockerl.snailchat.infrastructure.outbox.enums.EventType
import com.bockerl.snailchat.infrastructure.outbox.enums.OutboxStatus
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.*

@Document(collection = "outBox")
data class Outbox(
    @Id
    val outboxId: ObjectId = ObjectId.get(),
    val eventId: String = UUID.randomUUID().toString(),
    val aggregateId: String, // 연관된 도메인 객체의 식별자 (예: ChatMessage의 id를 String으로 변환)
    var eventType: EventType,
    var payload: String, // 이벤트 데이터를 JSON 등으로 직렬화한 문자열
    var status: OutboxStatus,
    var retryCount: Int = 0,
    @Indexed(unique = true)
    val idempotencyKey: String, // 중복 전파 방지를 위한 고유 키
    @CreatedDate
    var createdAt: Instant? = null,
    @LastModifiedDate
    var updatedAt: Instant? = null,
) : Persistable<ObjectId> {
    override fun getId(): ObjectId = outboxId

    // createdAt이 없으면 신규 객체로 간주하도록 처리
    override fun isNew(): Boolean = createdAt == null
}