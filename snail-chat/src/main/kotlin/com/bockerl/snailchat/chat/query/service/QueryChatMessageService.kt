package com.bockerl.snailchat.chat.query.service

import com.bockerl.snailchat.chat.query.dto.LatestChatMessageDto
import com.bockerl.snailchat.chat.query.dto.request.QueryChatMessageRequestDto
import com.bockerl.snailchat.chat.query.dto.response.QueryChatMessageResponseDto
import org.bson.types.ObjectId

interface QueryChatMessageService {
    fun getChatMessageByChatRoomId(queryChatMessageRequestDto: QueryChatMessageRequestDto): List<QueryChatMessageResponseDto>

    fun getIsFirstJoin(
        chatRoomId: String,
        memberId: String,
    ): Boolean

    fun getLatestChatMessageByChatRoomId(chatRoomId: ObjectId): LatestChatMessageDto?
}