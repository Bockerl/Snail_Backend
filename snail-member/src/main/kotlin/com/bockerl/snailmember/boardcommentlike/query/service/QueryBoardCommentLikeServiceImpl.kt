package com.bockerl.snailmember.boardcommentlike.query.service

import com.bockerl.snailmember.boardcommentlike.query.repository.BoardCommentLikeMapper
import jakarta.transaction.Transactional
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class QueryBoardCommentLikeServiceImpl(
    private val boardCommentLikeMapper: BoardCommentLikeMapper,
    private val redisTemplate: RedisTemplate<String, String>,
) : QueryBoardCommentLikeService {
    @Transactional
    override fun readBoardCommentLikeCount(boardCommentId: String): Long {
        val redisKey = "board-comment-like:count:$boardCommentId"
        val countStr = redisTemplate.opsForValue().get(redisKey)
        return countStr?.toLongOrNull() ?: 0L
    }

//    private fun needAdditionalBoard(
//        boardCommentId: String,
//        redisDataSize: Long,
//    ): Boolean {
//        val totalLikeCount = readDBLikeCountByBoardCommentId(boardCommentId)
//        val threshold = totalLikeCount * 0.9
//
//        return redisDataSize <= threshold
//    }

//    private fun readDBLikeCountByBoardCommentId(boardCommentId: String): Long =
//        boardCommentLikeMapper.selectCountByBoardCommentId(extractDigits(boardCommentId))

    private fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()
}