package com.bockerl.snailmember.boardlike.command.application.dto

data class CommandBoardLikeEventDTO(
    val boardId: String,
    val memberId: String,
    val eventId: String,
    val idempotencyKey: String,
)