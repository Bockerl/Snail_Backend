package com.bockerl.snailchat.chat.command.domain.repository

import com.bockerl.snailchat.chat.command.domain.aggregate.entity.PersonalChatRoom
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface CommandPersonalChatRoomRepository : MongoRepository<PersonalChatRoom, ObjectId>