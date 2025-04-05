package com.bockerl.snailchat.chat.command.domain.aggregate.event

import com.bockerl.snailchat.chat.command.domain.aggregate.enums.ChatMessageType

data class CommandSendMessageEvent(
    val chatMessageId: String,
    val chatRoomId: String,
    val memberId: String,
    val memberNickname: String,
    val memberPhoto: String,
    val message: String?,
    val messageType: ChatMessageType,
)