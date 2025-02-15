/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */

package com.bockerl.snailmember.board.query.service

import com.bockerl.snailmember.board.query.vo.QueryBoardResponseVO

interface QueryBoardService {
    fun readBoardByBoardId(boardId: String): QueryBoardResponseVO

    fun readBoardByBoardType(boardType: String): List<QueryBoardResponseVO>

    fun readBoardByBoardTag(boardTagList: List<String>): List<QueryBoardResponseVO>
}