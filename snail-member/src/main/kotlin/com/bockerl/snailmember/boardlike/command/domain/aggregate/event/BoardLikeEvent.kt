package com.bockerl.snailmember.boardlike.command.domain.aggregate.event

import com.bockerl.snailmember.boardlike.command.domain.aggregate.enum.ActionType

data class BoardLikeEvent(
    val boardId: String,
    val memberId: String,
    val actionType: ActionType,
)