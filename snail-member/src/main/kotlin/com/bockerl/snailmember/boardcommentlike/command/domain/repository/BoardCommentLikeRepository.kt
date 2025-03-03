package com.bockerl.snailmember.boardcommentlike.command.domain.repository

import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.entity.BoardCommentLike
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BoardCommentLikeRepository : JpaRepository<BoardCommentLike, Long> {
//    fun findMemberIdsByBoardId(boardId: String): List<BoardCommentLike>
//
//    fun findBoardIdsByMemberId(memberId: String): List<BoardCommentLike>
//
//    fun findByMemberIdAndBoardId(
//        memberId: String,
//        boardId: String,
//    ): BoardCommentLike?

    fun deleteByMemberIdAndBoardCommentId(
        memberId: Long,
        boardCommentId: Long,
    )
//
//    fun countByBoardId(boardId: String): Long
//
//    fun countByMemberId(memberId: String): Long
}