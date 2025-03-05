package com.bockerl.snailmember.boardlike.query.repository

import com.bockerl.snailmember.boardlike.query.dto.QueryBoardLikeDTO
import org.apache.ibatis.annotations.Mapper

@Mapper
interface BoardLikeMapper {
    fun selectMemberIdsByBoardId(boardId: Long): List<QueryBoardLikeDTO>

    fun selectBoardIdsByMemberId(memberId: Long): List<QueryBoardLikeDTO>

    fun selectByMemberIdAndBoardId(
        memberId: Long,
        boardId: Long,
    ): QueryBoardLikeDTO?

    fun selectCountByMemberId(memberId: Long): Long

    fun selectCountByBoardId(boardId: Long): Long
}