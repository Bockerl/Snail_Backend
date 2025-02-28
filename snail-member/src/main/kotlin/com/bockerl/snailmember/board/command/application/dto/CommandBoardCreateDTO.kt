/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */

package com.bockerl.snailmember.board.command.application.dto

import com.bockerl.snailmember.board.command.domain.aggregate.enums.BoardTag
import com.bockerl.snailmember.board.command.domain.aggregate.enums.BoardType

data class CommandBoardCreateDTO(
    val boardContents: String?,
    val boardType: BoardType,
    val boardTag: BoardTag,
    val boardLocation: String,
    val boardAccessLevel: String,
    val memberId: String,
)