/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */

package com.bockerl.snailmember.board.query.service

import com.bockerl.snailmember.board.query.vo.QueryBoardResponseVO

interface QueryBoardService {
    fun readBoardByBoardId(boardId: String): QueryBoardResponseVO

    fun readBoardByBoardType(
        boardType: String,
        lastId: Long?,
        pageSize: Int,
    ): List<QueryBoardResponseVO>

    fun readBoardByBoardTag(
        boardTagList: List<String>,
        lastId: Long?,
        pageSize: Int,
    ): List<QueryBoardResponseVO>
}