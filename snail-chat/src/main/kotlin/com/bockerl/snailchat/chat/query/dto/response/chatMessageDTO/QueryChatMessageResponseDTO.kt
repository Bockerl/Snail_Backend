package com.bockerl.snailchat.chat.query.dto.response.chatMessageDTO

import com.bockerl.snailchat.chat.command.domain.aggregate.enums.ChatMessageType
import java.time.Instant

data class QueryChatMessageResponseDTO(
    val chatMessageId: String,
    val chatRoomId: String,
    val memberId: String,
    val memberNickname: String,
    val memberPhoto: String,
    val message: String?,
    val messageType: ChatMessageType,
    val createdAt: Instant?,
)