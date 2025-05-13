package com.bockerl.snailmember.boardrecomment.command.application.dto

class CommandBoardRecommentCreateByGifDTO(
    val memberId: String,
    val boardId: String,
    val boardCommentId: String,
    val idempotencyKey: String,
)