package com.bockerl.snailchat.chat.query.service

import com.bockerl.snailchat.chat.query.dto.LatestChatMessageDTO
import com.bockerl.snailchat.chat.query.dto.request.QueryChatMessageRequestDTO
import com.bockerl.snailchat.chat.query.dto.response.QueryChatMessageResponseDTO
import org.bson.types.ObjectId

interface QueryChatMessageService {
    fun getChatMessageByChatRoomId(queryChatMessageRequestDto: QueryChatMessageRequestDTO): List<QueryChatMessageResponseDTO>

    fun getIsFirstJoin(
        chatRoomId: String,
        memberId: String,
    ): Boolean

    fun getLatestChatMessageByChatRoomId(chatRoomId: ObjectId): LatestChatMessageDTO?
}