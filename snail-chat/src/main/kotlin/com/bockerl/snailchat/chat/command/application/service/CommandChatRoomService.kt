package com.bockerl.snailchat.chat.command.application.service

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomCreateRequestDto

interface CommandChatRoomService {
    fun createChatRoom(commandChatRoomCreateRequestDto: CommandChatRoomCreateRequestDto) {
    }
}