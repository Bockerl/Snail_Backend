package com.bockerl.snailmember.boardrecommentlike.command.application.dto

data class CommandBoardRecommentLikeEventDTO(
    val boardCommentId: String,
    val boardId: String,
    val memberId: String,
    val boardRecommentId: String,
)