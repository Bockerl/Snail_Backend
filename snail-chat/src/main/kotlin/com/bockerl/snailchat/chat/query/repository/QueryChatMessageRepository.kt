package com.bockerl.snailchat.chat.query.repository

import com.bockerl.snailchat.chat.command.domain.aggregate.entity.ChatMessage
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

// @Repository
interface QueryChatMessageRepository :
    MongoRepository<ChatMessage, ObjectId>,
    QueryChatMessageCustomRepository