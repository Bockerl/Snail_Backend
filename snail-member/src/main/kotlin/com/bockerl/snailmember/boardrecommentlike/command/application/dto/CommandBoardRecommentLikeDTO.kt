package com.bockerl.snailmember.boardrecommentlike.command.application.dto

class CommandBoardRecommentLikeDTO(
    val boardCommentId: String,
    val boardId: String,
    val memberId: String,
    val boardRecommentId: String,
    val eventId: String? = null,
    val idempotencyKey: String,
)