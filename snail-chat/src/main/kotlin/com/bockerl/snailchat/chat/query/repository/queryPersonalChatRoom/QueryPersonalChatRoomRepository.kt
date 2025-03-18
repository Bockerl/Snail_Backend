package com.bockerl.snailchat.chat.query.repository.queryPersonalChatRoom

import com.bockerl.snailchat.chat.command.domain.aggregate.entity.PersonalChatRoom
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface QueryPersonalChatRoomRepository :
    MongoRepository<PersonalChatRoom, ObjectId>,
    QueryPersonalChatRoomCustomRepository