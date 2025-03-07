package com.bockerl.snailchat.chat.command.application.service

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomCreateRequestDto
import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomDeleteRequestDto

interface CommandChatRoomService {
    fun createChatRoom(commandChatRoomCreateRequestDto: CommandChatRoomCreateRequestDto) {
    }

    fun deleteChatRoom(commandChatRoomDeleteRequestDto: CommandChatRoomDeleteRequestDto) {
    }
}