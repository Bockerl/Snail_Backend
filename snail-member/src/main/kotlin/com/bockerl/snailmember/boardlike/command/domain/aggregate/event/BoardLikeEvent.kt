package com.bockerl.snailmember.boardlike.command.domain.aggregate.event

import com.bockerl.snailmember.boardlike.command.domain.aggregate.enums.BoardLikeActionType
import com.bockerl.snailmember.common.event.BaseLikeEvent

data class BoardLikeEvent(
    override val boardId: String,
    override val memberId: String,
    val boardLikeActionType: BoardLikeActionType,
) : BaseLikeEvent