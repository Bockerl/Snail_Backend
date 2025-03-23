package com.bockerl.snailchat.chat.query.dto.response

import com.bockerl.snailchat.chat.command.domain.aggregate.enums.ChatMessageType
import java.time.Instant

data class QueryChatMessageResponseDTO(
    val messageId: String, // _id 기반 페이지네이션을 위한 ObjectId 문자열로 반환
    val chatRoomId: String,
    val memberId: String,
    val memberNickname: String,
    val memberPhoto: String,
    val message: String?,
    val messageType: ChatMessageType,
    val createdAt: Instant?,
)