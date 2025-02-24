/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */

package com.bockerl.snailmember.board.command.domain.aggregate.vo.request

import io.swagger.v3.oas.annotations.media.Schema

data class CommandBoardDeleteRequestVO(
    @field:Schema(description = "게시글 번호", example = "BOA-00000001", type = "String")
    val boardId: String,
    @field:Schema(description = "회원번호", example = "MEM-00000001", type = "String")
    val memberId: String,
)