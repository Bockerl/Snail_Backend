package com.bockerl.snailchat.chat.command.application.dto.request

import com.bockerl.snailchat.chat.command.domain.aggregate.enums.ChatMessageType
import com.bockerl.snailchat.chat.command.domain.aggregate.enums.ChatRoomType

data class CommandChatMessageKeyRequestDTO(
    val chatRoomId: String,
    val memberId: String,
    val memberNickname: String,
    val memberPhoto: String,
    val chatRoomType: ChatRoomType,
    val message: String?,
    val messageType: ChatMessageType,
    val idempotencyKey: String,
)