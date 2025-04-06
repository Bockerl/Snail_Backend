package com.bockerl.snailchat.chat.query.service

import com.bockerl.snailchat.chat.query.dto.LatestChatMessageDTO
import com.bockerl.snailchat.chat.query.dto.request.chatMessageDTO.QueryChatMessageRequestDTO
import com.bockerl.snailchat.chat.query.dto.request.chatMessageDTO.QuerySearchChatMessageRequestDTO
import com.bockerl.snailchat.chat.query.dto.response.chatMessageDTO.QueryChatMessageResponseDTO
import com.bockerl.snailchat.chat.query.dto.response.chatMessageDTO.QuerySearchChatMessageResponseDTO
import org.bson.types.ObjectId

interface QueryChatMessageService {
    fun getChatMessageByChatRoomId(queryChatMessageRequestDTO: QueryChatMessageRequestDTO): List<QueryChatMessageResponseDTO>

    fun getIsFirstJoin(
        chatRoomId: String,
        memberId: String,
    ): Boolean

    fun getLatestChatMessageByChatRoomId(chatRoomId: ObjectId): LatestChatMessageDTO?

    fun searchChatMessageByKeyword(querySearchChatMessageRequestDTO: QuerySearchChatMessageRequestDTO): QuerySearchChatMessageResponseDTO
}