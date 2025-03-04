package com.bockerl.snailmember.board.command.domain.aggregate.vo.request

import com.bockerl.snailmember.board.command.domain.aggregate.enums.BoardTag
import com.bockerl.snailmember.board.command.domain.aggregate.enums.BoardType
import io.swagger.v3.oas.annotations.media.Schema


data class BoardRequestVO(
    @field:Schema(description = "게시글 내용", example = "달팽이 좋아요", type = "String")
    val boardContents: String? = null,
    @field:Schema(description = "게시글 타입", example = "FREE", type = "String")
    val boardType: BoardType? = null,
    @field:Schema(description = "게시글 태그", example = "TIP", type = "String")
    val boardTag: BoardTag? = null,
    @field:Schema(description = "게시글 지역", example = "SINDAEBANG", type = "String")
    val boardLocation: String? = null,
    @field:Schema(description = "게시글 공개 범위", example = "ALL", type = "String")
    val boardAccessLevel: String? = null,
    @field:Schema(description = "회원번호", example = "1", type = "Long")
    val memberId: Long? = null,
)