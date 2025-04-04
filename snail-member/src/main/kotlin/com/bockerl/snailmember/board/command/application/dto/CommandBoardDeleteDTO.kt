package com.bockerl.snailmember.board.command.application.dto

data class CommandBoardDeleteDTO(
    val boardId: String,
    val memberId: String,
    val idempotencyKey: String,
)