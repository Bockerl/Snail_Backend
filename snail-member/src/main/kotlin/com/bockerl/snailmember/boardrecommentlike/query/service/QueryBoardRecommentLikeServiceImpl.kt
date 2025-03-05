package com.bockerl.snailmember.boardrecommentlike.query.service

import com.bockerl.snailmember.boardrecommentlike.query.repository.BoardRecommentLikeMapper
import jakarta.transaction.Transactional
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class QueryBoardRecommentLikeServiceImpl(
    private val boardRecommentLikeMapper: BoardRecommentLikeMapper,
    private val redisTemplate: RedisTemplate<String, String>,
) : QueryBoardRecommentLikeService {
    @Transactional
    override fun readBoardRecommentLikeCount(boardRecommentId: String): Long {
        val redisKey = "board-recomment-like:count:$boardRecommentId"
        val countStr = redisTemplate.opsForValue().get(redisKey)
        return countStr?.toLongOrNull() ?: 0L
    }
}