package com.bockerl.snailmember.boardcomment.command.domain.aggregate.vo.request

import io.swagger.v3.oas.annotations.media.Schema

data class CommandBoardCommentDeleteRequestVO(
    @field:Schema(description = "게시글 댓글 번호", example = "BOA-COM-00000001", type = "String")
    val boardCommentId: String,
    @field:Schema(description = "회원 번호", example = "MEM-00000001", type = "String")
    val memberId: String,
    @field:Schema(description = "게시글 번호", example = "BOA-00000001", type = "String")
    val boardId: String,
)