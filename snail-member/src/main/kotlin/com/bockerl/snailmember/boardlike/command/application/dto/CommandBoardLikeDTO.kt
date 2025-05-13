package com.bockerl.snailmember.boardlike.command.application.dto

data class CommandBoardLikeDTO(
    val boardId: String,
    val memberId: String,
    val idempotencyKey: String,
)