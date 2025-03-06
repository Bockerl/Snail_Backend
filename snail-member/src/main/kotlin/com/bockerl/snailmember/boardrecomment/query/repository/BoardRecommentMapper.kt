package com.bockerl.snailmember.boardrecomment.query.repository

import com.bockerl.snailmember.boardrecomment.query.dto.QueryBoardRecommentDTO
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface BoardRecommentMapper {
    fun selectBoardRecommentsByBoardCommentId(
        @Param("boardCommentId") boardCommentId: Long,
        @Param("lastId") lastId: Long?,
        @Param("pageSize") pageSize: Int,
    ): List<QueryBoardRecommentDTO>

    fun selectBoardRecommentsByMemberId(
        @Param("memberId") memberId: Long,
        @Param("lastId") lastId: Long?,
        @Param("pageSize") pageSize: Int,
    ): List<QueryBoardRecommentDTO>
}