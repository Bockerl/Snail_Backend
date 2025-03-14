package com.bockerl.snailchat.chat.command.application.service

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomCreateRequestDto
import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomDeleteRequestDto

interface CommandChatRoomService {
    fun createPersonalChatRoom(commandChatRoomCreateRequestDto: CommandChatRoomCreateRequestDto) {
    }

    fun createGroupChatRoom(commandChatRoomCreateRequestDto: CommandChatRoomCreateRequestDto) {
    }

    fun deletePersonalChatRoom(commandChatRoomDeleteRequestDto: CommandChatRoomDeleteRequestDto) {
    }

    fun deleteGroupChatRoom(commandChatRoomDeleteRequestDto: CommandChatRoomDeleteRequestDto) {
    }
}