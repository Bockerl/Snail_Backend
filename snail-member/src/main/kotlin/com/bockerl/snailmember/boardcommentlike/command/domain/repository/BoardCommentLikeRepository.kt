package com.bockerl.snailmember.boardcommentlike.command.domain.repository

import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.entity.BoardCommentLike
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface BoardCommentLikeRepository : MongoRepository<BoardCommentLike, String> {
    fun findMemberIdsByBoardId(boardId: String): List<BoardCommentLike>

    fun findBoardIdsByMemberId(memberId: String): List<BoardCommentLike>

    fun findByMemberIdAndBoardId(
        memberId: String,
        boardId: String,
    ): BoardCommentLike?

    fun deleteByMemberIdAndBoardId(
        memberId: String,
        boardId: String,
    )

    fun countByBoardId(boardId: String): Long

    fun countByMemberId(memberId: String): Long
}