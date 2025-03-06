package com.bockerl.snailmember.boardrecommentlike.command.domain.aggregate.event

import com.bockerl.snailmember.boardrecommentlike.command.domain.aggregate.enums.BoardRecommentLikeActionType
import com.bockerl.snailmember.common.event.BaseLikeEvent

class BoardRecommentLikeEvent(
    override val boardId: String,
    override val memberId: String,
    val boardCommentId: String,
    val boardRecommentId: String,
    val boardRecommentLikeActionType: BoardRecommentLikeActionType,
) : BaseLikeEvent