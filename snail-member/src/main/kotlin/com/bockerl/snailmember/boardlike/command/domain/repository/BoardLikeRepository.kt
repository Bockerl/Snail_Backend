package com.bockerl.snailmember.boardlike.command.domain.repository

import com.bockerl.snailmember.boardlike.command.domain.aggregate.entity.BoardLike
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface BoardLikeRepository : MongoRepository<BoardLike, String> {
    fun findMemberIdsByBoardId(boardId: String): List<BoardLike>

    fun findBoardIdsByMemberId(memberId: String): List<BoardLike>

    fun findByMemberIdAndBoardId(
        memberId: String,
        boardId: String,
    ): BoardLike?

    fun deleteByMemberIdAndBoardId(
        memberId: String,
        boardId: String,
    )

    fun countByBoardId(boardId: String): Long

    fun countByMemberId(memberId: String): Long
}