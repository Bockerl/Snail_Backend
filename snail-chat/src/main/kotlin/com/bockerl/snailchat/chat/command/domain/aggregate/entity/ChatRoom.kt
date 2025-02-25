package com.bockerl.snailchat.chat.command.domain.aggregate.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "chatRoom")
class ChatRoom(
    @Id
    val chatRoomId: String? = null,
    val roomName: String,
    val participants: List<String>,
) {
    @CreatedDate
    lateinit var createdAt: LocalDateTime
}