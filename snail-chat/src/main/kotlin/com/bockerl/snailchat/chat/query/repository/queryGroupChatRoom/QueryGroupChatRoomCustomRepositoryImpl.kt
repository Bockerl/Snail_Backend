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

    override fun findGroupChatRoomsByMemberIdAndChatRoomNameContainingKeyword(
        memberId: String,
        keyword: String,
        limit: Int,
    ): List<GroupChatRoom> {
        val criteria =
            Criteria().andOperator(
                Criteria.where("participants.memberId").`is`(memberId),
                Criteria.where("chatRoomName").regex(".*$keyword.*", "i"), // 대소문자 무시 포함 검색
            )

        return mongoQueryUtil.findWithLimitAndSort(
            collection = GroupChatRoom::class.java,
            criteria = criteria,
            sortField = "updatedAt",
            limit = limit,
        )
    }
}