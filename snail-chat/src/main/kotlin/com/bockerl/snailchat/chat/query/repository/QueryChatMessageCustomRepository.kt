package com.bockerl.snailchat.chat.query.repository

import com.bockerl.snailchat.chat.command.domain.aggregate.entity.ChatMessage
import org.bson.types.ObjectId

interface QueryChatMessageCustomRepository {
    fun findLatestChatMessagesByChatRoomId(
        chatRoomId: ObjectId,
        pageSize: Int,
    ): List<ChatMessage>

    fun findPreviousChatMessagesByChatRoomId(
        chatRoomId: ObjectId,
        lastId: ObjectId,
        pageSize: Int,
    ): List<ChatMessage>
}