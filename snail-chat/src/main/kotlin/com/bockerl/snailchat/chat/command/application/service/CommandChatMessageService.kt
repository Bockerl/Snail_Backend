package com.bockerl.snailchat.chat.command.application.service

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDto

interface CommandChatMessageService {
    fun sendMessage(
        chatRoomId: String,
        updateMessageDto: CommandChatMessageRequestDto,
    )
}