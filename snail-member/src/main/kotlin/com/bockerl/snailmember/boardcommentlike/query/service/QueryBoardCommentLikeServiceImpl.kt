package com.bockerl.snailmember.boardcommentlike.query.service

import com.bockerl.snailmember.boardcommentlike.query.repository.BoardCommentLikeMapper
import jakarta.transaction.Transactional
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class QueryBoardCommentLikeServiceImpl(
    private val boardCommentLikeMapper: BoardCommentLikeMapper,
    private val redisTemplate: RedisTemplate<String, Any>,
) : QueryBoardCommentLikeService {
    @Transactional
    override fun readBoardCommentLikeCount(boardCommentId: String): Long {
        // 설명. 스크롤 할 때 보일 총 좋아요 수..
        // 설명. 게시글 댓글과 같이 rendering 하겠지..? 일단 보류
        val redisCount = redisTemplate.opsForSet().size("board-comment-like:$boardCommentId") ?: 0

        return if (needAdditionalBoard(boardCommentId, redisCount)) {
            readDBLikeCountByBoardCommentId(boardCommentId)
        } else {
            redisCount
        }
    }

    private fun needAdditionalBoard(
        boardCommentId: String,
        redisDataSize: Long,
    ): Boolean {
        val totalLikeCount = readDBLikeCountByBoardCommentId(boardCommentId)
        val threshold = totalLikeCount * 0.9

        return redisDataSize <= threshold
    }

    private fun readDBLikeCountByBoardCommentId(boardCommentId: String): Long =
        boardCommentLikeMapper.selectCountByBoardCommentId(extractDigits(boardCommentId))

    private fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()
}