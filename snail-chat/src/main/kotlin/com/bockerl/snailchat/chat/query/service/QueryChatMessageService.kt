package com.bockerl.snailchat.chat.query.service

import com.bockerl.snailchat.chat.query.dto.request.QueryChatMessageRequestDto
import com.bockerl.snailchat.chat.query.dto.response.QueryChatMessageResponseDto

interface QueryChatMessageService {
    fun getChatMessageByChatRoomId(queryChatMessageRequestDto: QueryChatMessageRequestDto): List<QueryChatMessageResponseDto>
}