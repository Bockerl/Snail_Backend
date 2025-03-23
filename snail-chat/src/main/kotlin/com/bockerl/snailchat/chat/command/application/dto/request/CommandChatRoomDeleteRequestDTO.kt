package com.bockerl.snailchat.chat.command.application.dto.request

data class CommandChatRoomDeleteRequestDTO(
    val chatRoomId: String,
    val memberId: String,
    val memberNickname: String,
    val memberPhoto: String,
)