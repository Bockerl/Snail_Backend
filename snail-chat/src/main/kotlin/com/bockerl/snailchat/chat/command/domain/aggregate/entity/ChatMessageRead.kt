@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailchat.chat.command.domain.aggregate.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "ChatMessageRead")
data class ChatMessageRead(
    @Id
    val readId: ObjectId = ObjectId.get(),
    val chatMessageId: ObjectId,
    val memberId: String,
    @CreatedDate
    val readAt: Instant = Instant.now(), // 이 메시지를 읽은 시점
) : Persistable<ObjectId> {
    override fun getId(): ObjectId = chatMessageId

    // createdAt이 없으면 신규 객체로 간주하여 @CreatedDate가 적용되도록 설정
    override fun isNew(): Boolean = readAt == null
}