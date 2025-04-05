package com.bockerl.snailchat.chat.command.domain.service

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageKeyRequestDTO
import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDTO
import com.bockerl.snailchat.chat.command.application.service.CommandChatMessageService
import com.bockerl.snailchat.chat.command.domain.aggregate.entity.ChatMessage
import com.bockerl.snailchat.chat.command.domain.aggregate.enums.ChatMessageType
import com.bockerl.snailchat.chat.command.domain.aggregate.event.CommandSendMessageEvent
import com.bockerl.snailchat.chat.command.domain.repository.CommandChatMessageRepository
import com.bockerl.snailchat.infrastructure.outbox.dto.OutboxDTO
import com.bockerl.snailchat.infrastructure.outbox.enums.EventType
import com.bockerl.snailchat.infrastructure.outbox.service.OutboxService
import com.bockerl.snailchat.infrastructure.producer.KafkaMessageProducer
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommandChatMessageServiceImpl(
    private val simpleMessagingTemplate: SimpMessagingTemplate,
    private val chatMessageRepository: CommandChatMessageRepository,
    private val kafkaMessageProducer: KafkaMessageProducer,
    private val objectMapper: ObjectMapper,
    private val outboxService: OutboxService,
    @Value("\${spring.kafka.topic.personal-chat}")
    private val personalChatTopic: String,
    @Value("\${spring.kafka.topic.group-chat}")
    private val groupChatTopic: String,
) : CommandChatMessageService {
    private val logger = KotlinLogging.logger { }

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

// Websocket + Stomp ---------------------------------------------------------------------------------------------------

    // Stomp + Kakfa를 통한 메시지 전송
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
        logger.info { "메시지 DB 저장=$chatMessage" }

        // Kafka Producer를 통해 personalChatTopic 토픽에 메시지 전송
        kafkaMessageProducer.sendMessageByKafka(personalChatTopic, updateMessageDTO.chatRoomId, updateMessageDTO)
    }

    // Consumer가 Kafka Broker에서 받아온 메시지를 Stomp를 통해서 내부에 전달
//    override fun sendToStomp(chatMessageDTO: CommandChatMessageRequestDTO) {
//        // 해당 경로를 구독하고 있는 Client들에게 Message 전송
//        simpleMessagingTemplate.convertAndSend("/topic/message/${chatMessageDTO.chatRoomId}", chatMessageDTO)
//    }

// Websocket + Stomp + Kafka  ----------------------------------------------------------------------------------------

    @Transactional
    override fun sendMessageByKafkaOutbox(updateMessageDTO: CommandChatMessageKeyRequestDTO) {
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
        val savedChatMessage = chatMessageRepository.save(chatMessage)
        logger.info { "메시지 DB 저장=$chatMessage" }

        // 멱등성 체크
        if (outboxService.existsByIdempotencyKey(updateMessageDTO.idempotencyKey)) {
            logger.info { "중복 요청: 이미 처리된 idempotencyKey = ${updateMessageDTO.idempotencyKey}" }
            return
        }

        // 도메인 이벤트 생성 (구독자에게 전달할 필요한 모든 정보 포함)
        val sendMessageEvent =
            CommandSendMessageEvent(
                chatMessageId = savedChatMessage.id.toHexString(),
                chatRoomId = savedChatMessage.chatRoomId.toHexString(),
                memberId = savedChatMessage.memberId,
                memberNickname = savedChatMessage.memberNickname,
                memberPhoto = savedChatMessage.memberPhoto,
                message = savedChatMessage.message,
                messageType = savedChatMessage.messageType,
            )

        // 이벤트를 JSON 문자열로 직렬화
        val jsonPayload = objectMapper.writeValueAsString(sendMessageEvent)

        // Outbox 엔티티 생성 및 idempotencyKey를 통한 중복 방지
        val outboxDTO =
            OutboxDTO(
                aggregateId = savedChatMessage.id.toHexString(), // objectId -> String
                eventType = EventType.PERSONAL_MESSAGE_SENT, // 예시: 메시지 전송 이벤트 타입
                payload = jsonPayload,
                idempotencyKey = updateMessageDTO.idempotencyKey, // 클라이언트에서 제공한 고유 키
            )

        // Outbox 등록
        outboxService.createOutbox(outboxDTO)
    }

    // Consumer가 Kafka Broker에서 받아온 메시지를 Stomp를 통해서 내부에 전달
    override fun sendToStomp(chatMessageDTO: CommandChatMessageRequestDTO) {
        // 해당 경로를 구독하고 있는 Client들에게 Message 전송
        simpleMessagingTemplate.convertAndSend("/topic/message/${chatMessageDTO.chatRoomId}", chatMessageDTO)
    }

// Websocket + Stomp + Kafka + Outbox --------------------------------------------------------------------------------

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