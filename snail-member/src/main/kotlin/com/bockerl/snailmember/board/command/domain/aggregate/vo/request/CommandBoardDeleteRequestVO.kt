package com.bockerl.snailmember.board.command.domain.aggregate.vo.request

import io.swagger.v3.oas.annotations.media.Schema

data class CommandBoardDeleteRequestVO(
    @field:Schema(description = "게시글 번호", example = "BOA-00000001", type = "String")
    val boardId: String,
    @field:Schema(description = "회원번호", example = "1", type = "Long")
    val memberId: Long,
)