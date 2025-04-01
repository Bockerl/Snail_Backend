package com.bockerl.snailmember.board.command.application.dto

import com.bockerl.snailmember.board.command.domain.aggregate.enums.BoardTag
import com.bockerl.snailmember.board.command.domain.aggregate.enums.BoardType

data class CommandBoardUpdateDTO(
    val boardId: String,
    val boardContents: String?,
    val boardType: BoardType,
    val boardTag: BoardTag,
    val boardLocation: String,
    val boardAccessLevel: String,
    val memberId: String,
    val deleteFilesIds: List<Long>,
    val idempotencyKey: String,
)