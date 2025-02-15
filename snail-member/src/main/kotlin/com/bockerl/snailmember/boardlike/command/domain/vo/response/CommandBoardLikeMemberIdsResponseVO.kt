package com.bockerl.snailmember.boardlike.command.domain.vo.response

import io.swagger.v3.oas.annotations.media.Schema

data class CommandBoardLikeMemberIdsResponseVO(
    @field:Schema(description = "멤버 닉네임", example = "sindaebang1", type = "String")
    val memberNickname: String?,
    @field:Schema(description = "회원 번호", example = "MEM-00000001", type = "String")
    val memberId: String?,
    @field:Schema(description = "회원 사진", example = "url", type = "String")
    val memberPhoto: String?,
)