package com.bockerl.snailmember.boardcommentlike.command.application.dto

data class CommandBoardCommentLikeEventDTO(
    val boardCommentId: String,
    val boardId: String,
    val memberId: String,
)