package com.bockerl.snailchat.chat.command.application.dto

import com.bockerl.snailchat.chat.command.domain.aggregate.enums.ChatMessageType

/**
 외부 메시지 브로커(Kafka)로 전송할 메시지 DTO
 내부 엔티티나 비즈니스 로직과 분리되어, 오직 메시지 전송에 필요한 데이터만 포함합니다.
 */
data class ChatMessageDTO(
    val chatRoomId: String,
    val memberId: String,
    val memberNickname: String,
    val memberPhoto: String,
    val message: String?,
    val messageType: ChatMessageType,
)