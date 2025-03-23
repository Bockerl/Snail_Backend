package com.bockerl.snailchat.chat.command.application.dto.request

data class CommandChatRoomCreateRequestDTO(
    val memberId: String,
    val memberNickname: String,
    val memberPhoto: String,
)