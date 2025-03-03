package com.bockerl.snailmember.boardcomment.command.application.dto

data class CommandBoardCommentDeleteDTO(
    val boardCommentId: String,
    val memberId: String,
    val boardId: String,
)