package com.bockerl.snailchat.chat.query.mapper

import com.bockerl.snailchat.chat.command.domain.aggregate.entity.ChatMessage
import com.bockerl.snailchat.chat.query.dto.LatestChatMessageDTO
import com.bockerl.snailchat.chat.query.dto.response.QueryChatMessageResponseDTO
import org.springframework.stereotype.Component

@Component
class EntityToDtoConverter {
    // 채팅방 메시지 Entity -> Dto
    fun chatMessageToQueryChatMessageResponseDto(entity: ChatMessage) =
        QueryChatMessageResponseDTO(
            messageId = entity.chatMessageId.toHexString(),
            chatRoomId = entity.chatRoomId.toHexString(),
            memberId = entity.memberId,
            memberNickname = entity.memberNickname,
            memberPhoto = entity.memberPhoto,
            message = entity.message ?: "",
            messageType = entity.messageType,
            createdAt = entity.createdAt,
        )

    // 채팅방 최신 메시지 Entity -> Dto
    fun latestChatMessageToLatestChatMessageDto(entity: ChatMessage) =
        LatestChatMessageDTO(
            message = entity.message ?: "",
            createdAt = entity.createdAt,
        )
}