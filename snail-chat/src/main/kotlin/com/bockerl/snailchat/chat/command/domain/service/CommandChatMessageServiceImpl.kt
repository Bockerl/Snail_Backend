package com.bockerl.snailchat.chat.command.domain.service

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDto
import com.bockerl.snailchat.chat.command.application.service.CommandChatMessageService
import com.bockerl.snailchat.chat.command.domain.aggregate.entity.ChatMessage
import com.bockerl.snailchat.chat.command.domain.aggregate.enums.ChatMessageType
import com.bockerl.snailchat.chat.command.domain.repository.CommandChatMessageRepository
import org.bson.types.ObjectId
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class CommandChatMessageServiceImpl(
    private val simpleMessagingTemplate: SimpMessagingTemplate,
    private val chatMessageRepository: CommandChatMessageRepository,
) : CommandChatMessageService {
    // STOMP를 통한 메시지 전송
    override fun sendMessage(
        chatRoomId: String,
        updateMessageDto: CommandChatMessageRequestDto,
    ) {
        // 전송할 메시지 옮기기
        val chatMessage =
            ChatMessage(
                chatRoomId = ObjectId(chatRoomId),
                memberId = updateMessageDto.memberId,
                memberNickname = updateMessageDto.memberNickname,
                memberPhoto = updateMessageDto.memberPhoto,
                message = updateMessageDto.message,
                messageType = updateMessageDto.messageType,
            )

        // 전송할 메시지 DB에 저장
        chatMessageRepository.save(chatMessage)

        // 해당 경로를 구독하고 있는 Client들에게 Message 전송
        simpleMessagingTemplate.convertAndSend("/topic/message/$chatRoomId", updateMessageDto)
    }

    override fun saveLeaveMessage(
        chatRoomId: String,
        memberId: String,
        memberNickname: String,
        memberPhoto: String,
    ) {
        val chatMessage =
            ChatMessage(
                chatRoomId = ObjectId(chatRoomId),
                memberId = memberId,
                memberNickname = memberNickname,
                memberPhoto = memberPhoto,
                message = "$memberNickname 님이 퇴장하셨습니다.",
                messageType = ChatMessageType.LEAVE,
            )

        chatMessageRepository.save(chatMessage)
    }
}