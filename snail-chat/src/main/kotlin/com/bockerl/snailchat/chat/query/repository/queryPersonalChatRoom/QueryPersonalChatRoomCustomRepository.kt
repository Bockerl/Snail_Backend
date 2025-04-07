package com.bockerl.snailchat.chat.query.repository.queryPersonalChatRoom

import com.bockerl.snailchat.chat.command.domain.aggregate.entity.PersonalChatRoom
import org.bson.types.ObjectId

interface QueryPersonalChatRoomCustomRepository {
    fun findLatestPersonalChatRoomsByMemberId(
        memberId: String,
        pageSize: Int,
    ): List<PersonalChatRoom>

    fun findPreviousPersonalChatRoomsByMemberId(
        memberId: String,
        lastId: ObjectId,
        pageSize: Int,
    ): List<PersonalChatRoom>

    fun findPersonalChatRoomsByMemberIdAndChatRoomNameContainingKeyword(
        memberId: String,
        keyword: String,
        limit: Int,
    ): List<PersonalChatRoom>
}