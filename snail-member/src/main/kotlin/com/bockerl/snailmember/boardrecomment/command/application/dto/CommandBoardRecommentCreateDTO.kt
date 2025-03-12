package com.bockerl.snailmember.boardrecomment.command.application.dto

class CommandBoardRecommentCreateDTO(
    val boardCommentContents: String,
    val memberId: String,
    val boardId: String,
    val boardCommentId: String,
)