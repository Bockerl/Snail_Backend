package com.bockerl.snailmember.boardlike.command.domain.repository

import com.bockerl.snailmember.boardlike.command.domain.aggregate.entity.BoardLike
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BoardLikeRepository : JpaRepository<BoardLike, Long> {
//    fun findMemberIdsByBoardId(boardId: String): List<BoardLike>
//
//    fun findBoardIdsByMemberId(memberId: String): List<BoardLike>
//
//    fun findByMemberIdAndBoardId(
//        memberId: String,
//        boardId: String,
//    ): BoardLike?

    fun deleteByMemberIdAndBoardId(
        memberId: Long,
        boardId: Long,
    )

//    fun countByBoardId(boardId: Long): Long
//
//    fun countByMemberId(memberId: Long): Long
}