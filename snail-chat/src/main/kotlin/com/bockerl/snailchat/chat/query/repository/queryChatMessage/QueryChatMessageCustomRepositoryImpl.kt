package com.bockerl.snailchat.chat.query.repository.queryChatMessage

import com.bockerl.snailchat.chat.command.domain.aggregate.entity.ChatMessage
import com.bockerl.snailchat.chat.query.repository.queryUtil.MongoQueryUtil
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
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

    override fun findChatMessagesByChatRoomIdAndMessageContainingKeyword(
        chatRoomId: ObjectId,
        keyword: String,
        page: Int,
        pageSize: Int,
    ): List<ChatMessage> {
        val criteria =
            Criteria
                .where("chatRoomId")
                .`is`(chatRoomId)
                .and("message")
                .regex(".*$keyword.*", "i") // 대소문자 구분 없이 포함 검색

        val skip = page * pageSize

        return mongoQueryUtil.findWithPagingSkip(
            collection = ChatMessage::class.java,
            criteria = criteria,
            sortField = "_id", // 최신순 정렬 (MongoDB ObjectId는 시간순)
            pageSize = pageSize,
            skip = skip,
            sortDirection = Sort.Direction.DESC,
        )
    }

    override fun countChatMessagesByChatRoomIdAndMessageContainingKeyword(
        chatRoomId: ObjectId,
        keyword: String,
    ): Long {
        val criteria =
            Criteria
                .where("chatRoomId")
                .`is`(chatRoomId)
                .and("message")
                .regex(".*$keyword.*", "i")

        return mongoQueryUtil.countDocuments(ChatMessage::class.java, criteria)
    }
}