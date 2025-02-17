/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */

package com.bockerl.snailmember.board.query.repository

import com.bockerl.snailmember.board.query.dto.QueryBoardDTO
import org.apache.ibatis.annotations.Mapper

@Mapper
interface BoardMapper {
    fun selectBoardByBoardId(boardId: Long): QueryBoardDTO?

    fun selectBoardByBoardType(boardType: String): List<QueryBoardDTO>?

    fun selectBoardByBoardTag(boardTagList: List<String>): List<QueryBoardDTO>?
}