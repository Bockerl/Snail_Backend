package com.bockerl.snailchat.chat.command.application.service

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDto

interface CommandChatMessageService {
    fun sendMessage(
        chatRoomId: String,
        updateMessageDto: CommandChatMessageRequestDto,
    )

    fun saveLeaveMessage(
        chatRoomId: String,
        memberId: String,
        memberNickname: String,
        memberPhoto: String,
    )
}