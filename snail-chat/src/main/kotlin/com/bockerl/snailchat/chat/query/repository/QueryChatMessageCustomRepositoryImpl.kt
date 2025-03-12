package com.bockerl.snailchat.chat.query.repository

import com.bockerl.snailchat.chat.command.domain.aggregate.entity.ChatMessage
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class QueryChatMessageCustomRepositoryImpl(
    private val mongoTemplate: MongoTemplate,
) : QueryChatMessageCustomRepository {
    override fun findLatestChatMessagesByChatRoomId(
        chatRoomId: ObjectId,
        pageSize: Int,
    ): List<ChatMessage> {
        val query =
            Query
                .query(Criteria.where("chatRoomId").`is`(chatRoomId))
                .with(Sort.by(Sort.Direction.DESC, "_id")) // 최신 메시지 우선 정렬
                .limit(pageSize) // 페이지 크기 제한

        return mongoTemplate.find(query, ChatMessage::class.java)
    }

    override fun findPreviousChatMessagesByChatRoomId(
        chatRoomId: ObjectId,
        lastId: ObjectId,
        pageSize: Int,
    ): List<ChatMessage> {
        val query =
            Query
                .query(
                    Criteria
                        .where("chatRoomId")
                        .`is`(chatRoomId)
                        .and("_id")
                        .lt(lastId), // lastId보다 작은 수만 조회
                ).with(Sort.by(Sort.Direction.DESC, "_id"))
                .limit(pageSize)

        return mongoTemplate.find(query, ChatMessage::class.java)
    }
}