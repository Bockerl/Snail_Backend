package com.bockerl.snailmember.boardlike.command.domain.aggregate.vo.request

import io.swagger.v3.oas.annotations.media.Schema

class CommandBoardLikeRequestVO(
    @field:Schema(description = "게시글 번호", example = "BOA-00000001", type = "String")
    val boardId: String,
    @field:Schema(description = "회원 번호", example = "MEM-00000001", type = "String")
    val memberId: String,
)