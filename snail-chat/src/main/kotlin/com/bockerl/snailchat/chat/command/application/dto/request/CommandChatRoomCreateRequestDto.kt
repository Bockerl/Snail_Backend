package com.bockerl.snailchat.chat.command.application.dto.request

data class CommandChatRoomCreateRequestDto(
    val memberId: String,
    val memberNickname: String,
    val memberPhoto: String,
)