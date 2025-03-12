package com.bockerl.snailmember.boardrecommentlike.command.domain.repository

import com.bockerl.snailmember.boardrecommentlike.command.domain.aggregate.entity.BoardRecommentLike
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BoardRecommentLikeRepository : JpaRepository<BoardRecommentLike, Long> {
    fun deleteByMemberIdAndBoardRecommentId(
        memberId: Long,
        boardRecommentId: Long,
    )
}