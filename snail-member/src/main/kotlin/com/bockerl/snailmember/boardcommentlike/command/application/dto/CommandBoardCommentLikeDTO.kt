package com.bockerl.snailmember.boardcommentlike.command.application.dto

class CommandBoardCommentLikeDTO(
    val boardCommentId: String,
    val boardId: String,
    val memberId: String,
    val eventId: String? = null,
    val idempotencyKey: String,
)