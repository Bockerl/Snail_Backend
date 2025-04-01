package com.bockerl.snailchat.chat.command.domain.service

import com.bockerl.snailchat.chat.command.application.dto.ChatMessageDTO
import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDTO
import com.bockerl.snailchat.chat.command.application.service.CommandChatMessageService
import com.bockerl.snailchat.chat.command.domain.aggregate.entity.ChatMessage
import com.bockerl.snailchat.chat.command.domain.aggregate.enums.ChatMessageType
import com.bockerl.snailchat.chat.command.domain.repository.CommandChatMessageRepository
import com.bockerl.snailchat.infrastructure.producer.KafkaMessageProducer
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class CommandChatMessageServiceImpl(
    private val simpleMessagingTemplate: SimpMessagingTemplate,
    private val chatMessageRepository: CommandChatMessageRepository,
    private val kafkaMessageProducer: KafkaMessageProducer,
    @Value("\${spring.kafka.topic.personal-chat}")
    private val personalChatTopic: String,
    @Value("\${spring.kafka.topic.group-chat}")
    private val groupChatTopic: String,
) : CommandChatMessageService {
    // STOMP를 통한 메시지 전송
//    @Transactional
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

// Websocket + Stomp ---------------------------------------------------------------------------------------------------

    // Stomp + Kakfa를 통한 메시지 전송
//    @Transactional
    override fun sendMessageByKafka(updateMessageDTO: CommandChatMessageRequestDTO) {
        // 전송할 메시지 옮기기
        val chatMessageDTO =
            ChatMessageDTO(
                chatRoomId = updateMessageDTO.chatRoomId,
                memberId = updateMessageDTO.memberId,
                memberNickname = updateMessageDTO.memberNickname,
                memberPhoto = updateMessageDTO.memberPhoto,
                message = updateMessageDTO.message,
                messageType = updateMessageDTO.messageType,
            )

        // Kafka Producer를 통해 personalChatTopic 토픽에 메시지 전송
        kafkaMessageProducer.sendMessageByKafka(personalChatTopic, chatMessageDTO.chatRoomId, chatMessageDTO)
    }

    // Consumer가 Kafka Broker에서 받아온 메시지를 Stomp를 통해서 내부에 전달
    override fun sendToStomp(chatMessageDTO: ChatMessageDTO) {
        // 전송할 메시지 옮기기
        val chatMessage =
            ChatMessage(
                chatRoomId = ObjectId(chatMessageDTO.chatRoomId),
                memberId = chatMessageDTO.memberId,
                memberNickname = chatMessageDTO.memberNickname,
                memberPhoto = chatMessageDTO.memberPhoto,
                message = chatMessageDTO.message,
                messageType = chatMessageDTO.messageType,
            )

        // 전송할 메시지 DB에 저장
        chatMessageRepository.save(chatMessage)

        // 해당 경로를 구독하고 있는 Client들에게 Message 전송
        simpleMessagingTemplate.convertAndSend("/topic/message/${chatMessage.chatRoomId}", chatMessage)
    }

// Websocket + Stomp + Kafka  ----------------------------------------------------------------------------------------

//    @Transactional
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

//    @Transactional
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