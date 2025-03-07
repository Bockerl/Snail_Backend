package com.bockerl.snailchat.chat.command.domain.repository

import com.bockerl.snailchat.chat.command.domain.aggregate.entity.ChatRoom
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface CommandChatRoomRepository : MongoRepository<ChatRoom, String>