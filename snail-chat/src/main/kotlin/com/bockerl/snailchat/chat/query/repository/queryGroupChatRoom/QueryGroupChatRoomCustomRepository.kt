package com.bockerl.snailchat.chat.query.repository.queryGroupChatRoom

import com.bockerl.snailchat.chat.command.domain.aggregate.entity.GroupChatRoom
import org.bson.types.ObjectId

interface QueryGroupChatRoomCustomRepository {
    fun findLatestGroupChatRoomsByMemberId(
        memberId: String,
        pageSize: Int,
    ): List<GroupChatRoom>

    fun findPreviousGroupChatRoomsByMemberId(
        memberId: String,
        lastId: ObjectId,
        pageSize: Int,
    ): List<GroupChatRoom>
}