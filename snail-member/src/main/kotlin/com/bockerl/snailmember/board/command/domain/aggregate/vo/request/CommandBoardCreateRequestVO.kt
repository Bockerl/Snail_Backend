/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */

package com.bockerl.snailmember.board.command.domain.aggregate.vo.request

import com.bockerl.snailmember.board.command.domain.aggregate.enums.BoardTag
import com.bockerl.snailmember.board.command.domain.aggregate.enums.BoardType
import io.swagger.v3.oas.annotations.media.Schema

data class CommandBoardCreateRequestVO(
    @field:Schema(description = "게시글 내용", example = "달팽이 좋아요", type = "String")
    val boardContents: String?,
    @field:Schema(description = "게시글 타입", example = "FREE", type = "String")
    val boardType: BoardType,
    @field:Schema(description = "게시글 태그", example = "TIP", type = "String")
    val boardTag: BoardTag,
    @field:Schema(description = "게시글 지역", example = "SINDAEBANG", type = "String")
    val boardLocation: String,
    @field:Schema(description = "게시글 공개 범위", example = "ALL", type = "String")
    val boardAccessLevel: String,
    @field:Schema(description = "회원번호", example = "MEM-00000001", type = "String")
    val memberId: String,
)