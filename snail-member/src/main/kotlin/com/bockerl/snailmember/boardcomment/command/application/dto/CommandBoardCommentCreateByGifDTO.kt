package com.bockerl.snailmember.boardcomment.command.application.dto

data class CommandBoardCommentCreateByGifDTO(
    val memberId: String,
    val boardId: String,
    val idempotencyKey: String,
)