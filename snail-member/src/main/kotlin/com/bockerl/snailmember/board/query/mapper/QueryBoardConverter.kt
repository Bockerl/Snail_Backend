package com.bockerl.snailmember.board.query.mapper


import com.bockerl.snailmember.board.query.dto.QueryBoardDTO
import com.bockerl.snailmember.board.query.vo.QueryBoardResponseVO
import org.springframework.stereotype.Component

@Component
class QueryBoardConverter {
    fun dtoToResponseVO(dto: QueryBoardDTO): QueryBoardResponseVO =
        QueryBoardResponseVO(
            boardId = dto.formattedId,
            boardContents = dto.boardContents,
            queryBoardType = dto.boardType,
            queryBoardTag = dto.boardTag,
            boardLocation = dto.boardLocation,
            boardAccessLevel = dto.boardAccessLevel,
            boardView = dto.boardView,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
            active = dto.active,
            memberId = dto.memberId,
        )
}