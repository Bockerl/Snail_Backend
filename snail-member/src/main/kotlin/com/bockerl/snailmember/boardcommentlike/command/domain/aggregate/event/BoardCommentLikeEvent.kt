package com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.event

import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.enums.BoardCommentLikeActionType
import com.bockerl.snailmember.common.event.BaseLikeEvent

data class BoardCommentLikeEvent(
    override val boardId: String,
    override val memberId: String,
    val boardCommentId: String,
    val boardCommentLikeActionType: BoardCommentLikeActionType,
) : BaseLikeEvent