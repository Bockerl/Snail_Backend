package com.bockerl.snailchat.chat.query.mapper

import com.bockerl.snailchat.chat.command.domain.aggregate.entity.ChatMessage
import com.bockerl.snailchat.chat.query.dto.response.QueryChatMessageResponseDto
import org.springframework.stereotype.Component

@Component
class EntityToDtoConverter {
    // 채팅방 메시지 Entity -> Dto
    fun chatMessageToQueryChatMessageResponseDto(entity: ChatMessage) =
        QueryChatMessageResponseDto(
            messageId = entity.chatMessageId.toHexString(),
            chatRoomId = entity.chatRoomId.toHexString(),
            memberId = entity.memberId,
            memberNickname = entity.memberNickname,
            memberPhoto = entity.memberPhoto,
            message = entity.message ?: "",
            messageType = entity.messageType,
            createdAt = entity.createdAt,
        )
}