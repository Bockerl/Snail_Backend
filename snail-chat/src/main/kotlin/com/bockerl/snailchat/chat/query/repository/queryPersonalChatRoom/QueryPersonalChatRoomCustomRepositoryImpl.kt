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
        memberId: ObjectId,
        pageSize: Int,
    ): List<PersonalChatRoom> {
        val personalChatRoom =
            mongoQueryUtil.findWithPaging(
                PersonalChatRoom::class.java,
                Criteria.where("memberId").`is`(memberId),
                "_id",
                pageSize,
            )

        return personalChatRoom
    }

    override fun findPreviousPersonalChatRoomsByMemberId(
        memberId: ObjectId,
        lastId: ObjectId,
        pageSize: Int,
    ): List<PersonalChatRoom> {
        val personalChatRoom =
            mongoQueryUtil.findWithPaging(
                PersonalChatRoom::class.java,
                Criteria
                    .where("memberId")
                    .`is`(memberId)
                    .and("_id")
                    .lt(lastId), // lastId보다 작은 수만 조회
                "_id",
                pageSize,
            )

        return personalChatRoom
    }
}