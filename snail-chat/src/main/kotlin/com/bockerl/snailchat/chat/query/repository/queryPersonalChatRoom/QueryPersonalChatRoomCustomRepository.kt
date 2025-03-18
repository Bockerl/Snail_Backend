package com.bockerl.snailchat.chat.query.repository.queryPersonalChatRoom

import com.bockerl.snailchat.chat.command.domain.aggregate.entity.PersonalChatRoom
import org.bson.types.ObjectId

interface QueryPersonalChatRoomCustomRepository {
    fun findLatestPersonalChatRoomsByMemberId(
        memberId: ObjectId,
        pageSize: Int,
    ): List<PersonalChatRoom>

    fun findPreviousPersonalChatRoomsByMemberId(
        memberId: ObjectId,
        lastId: ObjectId,
        pageSize: Int,
    ): List<PersonalChatRoom>
}