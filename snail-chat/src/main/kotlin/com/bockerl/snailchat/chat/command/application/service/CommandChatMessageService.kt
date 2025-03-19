package com.bockerl.snailchat.chat.command.application.service

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDto
import org.bson.types.ObjectId

interface CommandChatMessageService {
    fun sendMessage(
        chatRoomId: String,
        updateMessageDto: CommandChatMessageRequestDto,
    )

    fun saveLeaveMessage(
        chatRoomId: ObjectId,
        memberId: String,
        memberNickname: String,
        memberPhoto: String,
    )

    fun saveEnterMessage(
        chatRoomId: ObjectId,
        memberId: String,
        memberNickname: String,
        memberPhoto: String,
    )
}