package com.bockerl.snailchat.chat.query.repository.queryGroupChatRoom

import com.bockerl.snailchat.chat.command.domain.aggregate.entity.GroupChatRoom
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface QueryGroupChatRoomRepository :
    MongoRepository<GroupChatRoom, ObjectId>,
    QueryGroupChatRoomCustomRepository