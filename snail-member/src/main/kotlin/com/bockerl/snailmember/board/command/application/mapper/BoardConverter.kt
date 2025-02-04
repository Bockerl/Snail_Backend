package com.bockerl.snailmember.board.command.application.mapper

import com.bockerl.snailmember.board.command.application.dto.BoardDTO
import com.bockerl.snailmember.board.command.domain.aggregate.entity.Board
import com.bockerl.snailmember.board.command.domain.aggregate.vo.response.BoardResponseVO
import org.springframework.stereotype.Component

@Component
class BoardConverter {
    // Entity to DTO 변환
    fun entityToDTO(entity: Board): BoardDTO =
        BoardDTO(
            boardId = entity.formattedId,
            boardContents = entity.boardContents,
            boardType = entity.boardType,
            boardTag = entity.boardTag,
            boardLocation = entity.boardLocation,
            boardAccessLevel = entity.boardAccessLevel,
            boardView = entity.boardView,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            active = entity.active,
            memberId = entity.memberId,
        )

    fun dtoToResponseVO(dto: BoardDTO): BoardResponseVO =
        BoardResponseVO(
            boardId = dto.boardId,
            boardContents = dto.boardContents,
            boardType = dto.boardType,
            boardTag = dto.boardTag,
            boardLocation = dto.boardLocation,
            boardAccessLevel = dto.boardAccessLevel,
            boardView = dto.boardView,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
            active = dto.active,
            memberId = dto.memberId,
        )
}