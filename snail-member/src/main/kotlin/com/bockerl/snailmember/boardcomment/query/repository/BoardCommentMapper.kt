package com.bockerl.snailmember.boardcomment.query.repository

import com.bockerl.snailmember.boardcomment.query.dto.QueryBoardCommentDTO
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface BoardCommentMapper {
    fun selectBoardCommentsByBoardId(
        @Param("boardId") boardId: Long,
        @Param("lastId") lastId: Long?,
        @Param("pageSize") pageSize: Int,
    ): List<QueryBoardCommentDTO>

    fun selectBoardCommentsByMemberId(
        @Param("memberId") memberId: Long,
        @Param("lastId") lastId: Long?,
        @Param("pageSize") pageSize: Int,
    ): List<QueryBoardCommentDTO>
}