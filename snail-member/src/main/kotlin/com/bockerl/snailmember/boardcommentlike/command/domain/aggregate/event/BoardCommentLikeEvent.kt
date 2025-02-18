package com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.event

import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.enum.BoardCommentLikeActionType

data class BoardCommentLikeEvent(
    val boardId: String,
    val memberId: String,
    val boardCommentId: String,
    val boardCommentLikeActionType: BoardCommentLikeActionType,
)