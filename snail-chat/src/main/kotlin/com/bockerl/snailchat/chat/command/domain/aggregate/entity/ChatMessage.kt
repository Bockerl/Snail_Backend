
@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailchat.chat.command.domain.aggregate.entity

import com.bockerl.snailchat.chat.command.domain.aggregate.enums.ChatMessageType
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "chatMessage")
data class ChatMessage(
    @Id
    val chatMessageId: ObjectId = ObjectId.get(),
    val chatRoomId: ObjectId = ObjectId.get(),
    val memberId: String,
    val memberNickname: String,
    val memberPhoto: String,
    val message: String?,
    val messageType: ChatMessageType,
    @CreatedDate
    var createdAt: Instant? = null,
    @LastModifiedDate
    var updatedAt: Instant? = null,
) : Persistable<ObjectId> {
    override fun getId(): ObjectId = chatMessageId

    // createdAt이 없으면 신규 객체로 간주하여 @CreatedDate가 적용되도록 설정
    override fun isNew(): Boolean = createdAt == null
}