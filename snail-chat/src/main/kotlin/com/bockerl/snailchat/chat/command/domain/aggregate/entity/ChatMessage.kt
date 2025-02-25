
@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailchat.chat.command.domain.aggregate.entity

import com.bockerl.snailchat.chat.command.domain.aggregate.enums.CommandChatMessageType
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "chatMessage")
data class ChatMessage(
    @Id
    val chatMessageId: String? = null,
    val chatRoomId: String,
    val sender: String,
    val message: String?,
    val messageType: CommandChatMessageType,
) {
    @CreatedDate
    lateinit var createdAt: LocalDateTime // mongoDB가 값을 넣을떄 초기화하면서 넣어줌
}