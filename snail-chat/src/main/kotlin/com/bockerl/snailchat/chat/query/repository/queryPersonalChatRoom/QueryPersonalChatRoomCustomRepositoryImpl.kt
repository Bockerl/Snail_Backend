package com.bockerl.snailchat.chat.query.repository.queryPersonalChatRoom

import com.bockerl.snailchat.chat.command.domain.aggregate.entity.PersonalChatRoom
import com.bockerl.snailchat.chat.query.repository.queryUtil.MongoQueryUtil
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Repository

@Repository
class QueryPersonalChatRoomCustomRepositoryImpl(
    private val mongoQueryUtil: MongoQueryUtil,
) : QueryPersonalChatRoomCustomRepository {
    override fun findLatestPersonalChatRoomsByMemberId(
        memberId: String,
        pageSize: Int,
    ): List<PersonalChatRoom> {
        val personalChatRoom =
            mongoQueryUtil.findWithPaging(
                PersonalChatRoom::class.java,
                Criteria.where("participants.memberId").`is`(memberId),
                "_id",
                pageSize,
            )

        return personalChatRoom
    }

    override fun findPreviousPersonalChatRoomsByMemberId(
        memberId: String,
        lastId: ObjectId,
        pageSize: Int,
    ): List<PersonalChatRoom> {
        val personalChatRoom =
            mongoQueryUtil.findWithPaging(
                PersonalChatRoom::class.java,
                Criteria
                    .where("participants.memberId")
                    .`is`(memberId)
                    .and("_id")
                    .lt(lastId), // lastId보다 작은 수만 조회
                "_id",
                pageSize,
            )

        return personalChatRoom
    }

    override fun findPersonalChatRoomsByMemberIdAndChatRoomNameContainingKeyword(
        memberId: String,
        keyword: String,
        limit: Int,
    ): List<PersonalChatRoom> {
        // 존재 여부 체크
        val criteria = Criteria.where("participants.memberId").`is`(memberId)

        // 채팅방 이름 (상대방 이름) 중 keyword 포함
        val regexCriteria = Criteria.where("chatRoomName.$memberId").regex(".*$keyword.*", "i")

        val finalCriteria = Criteria().andOperator(criteria, regexCriteria)

        return mongoQueryUtil.findWithLimitAndSort(
            collection = PersonalChatRoom::class.java,
            criteria = finalCriteria,
            sortField = "updatedAt",
            limit = limit,
        )
    }
}