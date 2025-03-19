package com.bockerl.snailchat.chat.query.repository.queryGroupChatRoom

import com.bockerl.snailchat.chat.command.domain.aggregate.entity.GroupChatRoom
import com.bockerl.snailchat.chat.query.repository.queryUtil.MongoQueryUtil
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Repository

@Repository
class QueryGroupChatRoomCustomRepositoryImpl(
    private val mongoQueryUtil: MongoQueryUtil,
) : QueryGroupChatRoomCustomRepository {
    override fun findLatestGroupChatRoomsByMemberId(
        memberId: String,
        pageSize: Int,
    ): List<GroupChatRoom> {
        val groupChatRoom =
            mongoQueryUtil.findWithPaging(
                GroupChatRoom::class.java,
                Criteria.where("participants.memberId").`is`(memberId),
                "_id",
                pageSize,
            )

        return groupChatRoom
    }

    override fun findPreviousGroupChatRoomsByMemberId(
        memberId: String,
        lastId: ObjectId,
        pageSize: Int,
    ): List<GroupChatRoom> {
        val groupChatRoom =
            mongoQueryUtil.findWithPaging(
                GroupChatRoom::class.java,
                Criteria
                    .where("participants.memberId")
                    .`is`(memberId)
                    .and("_id")
                    .lt(lastId), // lastId보다 작은 수만 조회
                "_id",
                pageSize,
            )

        return groupChatRoom
    }
}