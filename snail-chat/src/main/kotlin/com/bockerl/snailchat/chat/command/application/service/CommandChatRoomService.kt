package com.bockerl.snailchat.chat.command.application.service

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomRequestDto

interface CommandChatRoomService {
    fun createChatRoom(commandChatRoomRequestDto: CommandChatRoomRequestDto) {
    }
}