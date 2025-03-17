package com.bockerl.snailchat.chat.query.repository

import com.bockerl.snailchat.chat.command.domain.aggregate.entity.ChatMessage
import com.bockerl.snailchat.chat.command.domain.aggregate.enums.ChatMessageType
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

// @Repository
interface QueryChatMessageRepository :
    MongoRepository<ChatMessage, ObjectId>,
    QueryChatMessageCustomRepository {
    fun findTopByChatRoomIdAndMemberIdAndMessageTypeOrderByCreatedAtDesc(
        chatRoomId: ObjectId,
        memberId: String,
        leave: ChatMessageType,
    ): ChatMessage?
}