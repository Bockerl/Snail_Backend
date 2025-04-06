package com.bockerl.snailmember.boardlike.query.repository

import com.bockerl.snailmember.boardlike.query.dto.QueryBoardLikeDTO
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface BoardLikeMapper {
    fun selectMemberIdsByBoardId(
        @Param("boardId") boardId: Long,
        @Param("lastId") lastId: Long?,
        @Param("pageSize") pageSize: Int,
    ): List<QueryBoardLikeDTO>

    fun selectBoardIdsByMemberId(
        @Param("memberId") memberId: Long,
        @Param("lastId") lastId: Long?,
        @Param("pageSize") pageSize: Int,
    ): List<QueryBoardLikeDTO>

    fun selectByMemberIdAndBoardId(
        memberId: Long,
        boardId: Long,
    ): QueryBoardLikeDTO?

    fun selectCountByMemberId(memberId: Long): Long

    fun selectCountByBoardId(boardId: Long): Long
}