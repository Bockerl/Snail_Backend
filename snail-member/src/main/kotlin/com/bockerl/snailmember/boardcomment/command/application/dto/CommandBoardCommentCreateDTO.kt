package com.bockerl.snailmember.boardcomment.command.application.dto

class CommandBoardCommentCreateDTO(
    val boardCommentContents: String,
    val memberId: String,
    val boardId: String,
    val idempotencyKey: String,
)