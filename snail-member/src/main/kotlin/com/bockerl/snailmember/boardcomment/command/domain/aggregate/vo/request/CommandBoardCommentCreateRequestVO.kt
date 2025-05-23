package com.bockerl.snailmember.boardcomment.command.domain.aggregate.vo.request

import io.swagger.v3.oas.annotations.media.Schema

data class CommandBoardCommentCreateRequestVO(
    @field:Schema(description = "게시글 댓글 내용 ", example = "와 최고에요~!", type = "String")
    val boardCommentContents: String,
    @field:Schema(description = "회원 번호", example = "MEM-00000001", type = "String")
    val memberId: String,
    @field:Schema(description = "게시글 번호", example = "BOA-00000001", type = "String")
    val boardId: String,
)