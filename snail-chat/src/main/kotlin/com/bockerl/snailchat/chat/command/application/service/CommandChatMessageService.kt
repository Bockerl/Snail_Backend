package com.bockerl.snailchat.chat.command.application.service

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageKeyRequestDTO
import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDTO
import org.bson.types.ObjectId

interface CommandChatMessageService {
    fun sendMessage(
//        chatRoomId: String,
        updateMessageDTO: CommandChatMessageRequestDTO,
    )

    fun sendMessageByKafka(
//        chatRoomId: String,
        updateMessageDTO: CommandChatMessageRequestDTO,
    )

    fun sendToStomp(chatMessageDTO: CommandChatMessageRequestDTO)

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

    fun sendMessageByKafkaOutbox(updateMessageDTO: CommandChatMessageKeyRequestDTO)
}