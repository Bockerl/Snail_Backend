package com.bockerl.snailmember.boardlike.command.domain.aggregate.event

import com.bockerl.snailmember.boardlike.command.domain.aggregate.enum.BoardLikeActionType

data class BoardLikeEvent(
    val boardId: String,
    val memberId: String,
    val boardLikeActionType: BoardLikeActionType,
)