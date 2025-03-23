package com.bockerl.snailchat.chat.command.domain.service

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDTO
import com.bockerl.snailchat.chat.command.application.service.CommandChatMessageService
import com.bockerl.snailchat.chat.command.domain.aggregate.entity.ChatMessage
import com.bockerl.snailchat.chat.command.domain.aggregate.enums.ChatMessageType
import com.bockerl.snailchat.chat.command.domain.repository.CommandChatMessageRepository
import org.bson.types.ObjectId
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommandChatMessageServiceImpl(
    private val simpleMessagingTemplate: SimpMessagingTemplate,
    private val chatMessageRepository: CommandChatMessageRepository,
) : CommandChatMessageService {
    // STOMP를 통한 메시지 전송
    @Transactional
    override fun sendMessage(updateMessageDTO: CommandChatMessageRequestDTO) {
        // 전송할 메시지 옮기기
        val chatMessage =
            ChatMessage(
                chatRoomId = ObjectId(updateMessageDTO.chatRoomId),
                memberId = updateMessageDTO.memberId,
                memberNickname = updateMessageDTO.memberNickname,
                memberPhoto = updateMessageDTO.memberPhoto,
                message = updateMessageDTO.message,
                messageType = updateMessageDTO.messageType,
            )

        // 전송할 메시지 DB에 저장
        chatMessageRepository.save(chatMessage)

        // 해당 경로를 구독하고 있는 Client들에게 Message 전송
        simpleMessagingTemplate.convertAndSend("/topic/message/${chatMessage.chatRoomId}", chatMessage)
    }

    // Kakfa를 통한 메시지 전송
    @Transactional
    override fun sendMessageByKafka(updateMessageDTO: CommandChatMessageRequestDTO) {
        // 전송할 메시지 옮기기
        val chatMessage =
            ChatMessage(
                chatRoomId = ObjectId(updateMessageDTO.chatRoomId),
                memberId = updateMessageDTO.memberId,
                memberNickname = updateMessageDTO.memberNickname,
                memberPhoto = updateMessageDTO.memberPhoto,
                message = updateMessageDTO.message,
                messageType = updateMessageDTO.messageType,
            )

        // 전송할 메시지 DB에 저장
        chatMessageRepository.save(chatMessage)

        // 해당 경로를 구독하고 있는 Client들에게 Message 전송
        simpleMessagingTemplate.convertAndSend("/topic/message/${chatMessage.chatRoomId}", chatMessage)

        // Kafka에 event 발행
//        sendMessageEvent =
//            CommandSendMessageEvent()
    }

    @Transactional
    override fun saveLeaveMessage(
        chatRoomId: ObjectId,
        memberId: String,
        memberNickname: String,
        memberPhoto: String,
    ) {
        val chatMessage =
            ChatMessage(
                chatRoomId = chatRoomId,
                memberId = memberId,
                memberNickname = memberNickname,
                memberPhoto = memberPhoto,
                message = "$memberNickname 님이 퇴장하셨습니다.",
                messageType = ChatMessageType.LEAVE,
            )

        chatMessageRepository.save(chatMessage)
    }

    @Transactional
    override fun saveEnterMessage(
        chatRoomId: ObjectId,
        memberId: String,
        memberNickname: String,
        memberPhoto: String,
    ) {
        val enterMessage =
            ChatMessage(
                chatRoomId = chatRoomId,
                memberId = memberId,
                memberNickname = memberNickname,
                memberPhoto = memberPhoto,
                message = "${memberNickname}님이 입장하셨습니다.",
                messageType = ChatMessageType.ENTER,
            )

        chatMessageRepository.save(enterMessage)
    }
}