package com.bockerl.snailchat.chat.command.application.service

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomCreateRequestDTO
import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomDeleteRequestDTO
import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomJoinRequestDTO

interface CommandChatRoomService {
    fun createPersonalChatRoom(commandChatRoomCreateRequestDto: CommandChatRoomCreateRequestDTO)

    fun createGroupChatRoom(commandChatRoomCreateRequestDto: CommandChatRoomCreateRequestDTO)

    fun deletePersonalChatRoom(commandChatRoomDeleteRequestDto: CommandChatRoomDeleteRequestDTO)

    fun deleteGroupChatRoom(commandChatRoomDeleteRequestDto: CommandChatRoomDeleteRequestDTO)

    fun joinGroupChatRoom(commandChatRoomJoinRequestDto: CommandChatRoomJoinRequestDTO)
}