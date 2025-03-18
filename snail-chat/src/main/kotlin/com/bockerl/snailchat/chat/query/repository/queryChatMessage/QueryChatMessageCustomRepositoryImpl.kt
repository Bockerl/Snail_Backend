package com.bockerl.snailchat.chat.query.repository.queryChatMessage

import com.bockerl.snailchat.chat.command.domain.aggregate.entity.ChatMessage
import com.bockerl.snailchat.chat.query.repository.queryUtil.MongoQueryUtil
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Repository

@Repository
class QueryChatMessageCustomRepositoryImpl(
    private val mongoQueryUtil: MongoQueryUtil,
) : QueryChatMessageCustomRepository {
    override fun findLatestChatMessagesByChatRoomId(
        chatRoomId: ObjectId,
        pageSize: Int,
    ): List<ChatMessage> {
        val chatMessages =
            mongoQueryUtil.findWithPaging(
                ChatMessage::class.java,
                Criteria.where("chatRoomId").`is`(chatRoomId),
                "_id",
                pageSize,
            )

        return chatMessages
    }

    override fun findPreviousChatMessagesByChatRoomId(
        chatRoomId: ObjectId,
        lastId: ObjectId,
        pageSize: Int,
    ): List<ChatMessage> {
        val chatMessages =
            mongoQueryUtil.findWithPaging(
                ChatMessage::class.java,
                Criteria
                    .where("chatRoomId")
                    .`is`(chatRoomId)
                    .and("_id")
                    .lt(lastId), // lastId보다 작은 수만 조회
                "_id",
                pageSize,
            )

        return chatMessages
    }
}