package com.bockerl.snailchat.chat.command.domain.repository

import com.bockerl.snailchat.chat.command.domain.aggregate.entity.GroupChatRoom
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface CommandGroupChatRoomRepository : MongoRepository<GroupChatRoom, ObjectId>