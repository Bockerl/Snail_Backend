package com.bockerl.snailchat.chat.command.application.service

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomCreateRequestDTO
import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomDeleteRequestDTO
import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomJoinRequestDTO

interface CommandChatRoomService {
    fun createPersonalChatRoom(commandChatRoomCreateRequestDTO: CommandChatRoomCreateRequestDTO)

    fun createGroupChatRoom(commandChatRoomCreateRequestDTO: CommandChatRoomCreateRequestDTO)

    fun deletePersonalChatRoom(commandChatRoomDeleteRequestDTO: CommandChatRoomDeleteRequestDTO)

    fun deleteGroupChatRoom(commandChatRoomDeleteRequestDTO: CommandChatRoomDeleteRequestDTO)

    fun joinGroupChatRoom(commandChatRoomJoinRequestDTO: CommandChatRoomJoinRequestDTO)
}