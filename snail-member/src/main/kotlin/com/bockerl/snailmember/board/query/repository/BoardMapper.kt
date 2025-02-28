/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */

package com.bockerl.snailmember.board.query.repository

import com.bockerl.snailmember.board.query.dto.QueryBoardDTO
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface BoardMapper {
    fun selectBoardByBoardId(boardId: Long): QueryBoardDTO?

    fun selectBoardByBoardType(
        @Param("boardType") boardType: String,
        @Param("lastId") lastId: Long?,
        @Param("pageSize") pageSize: Int,
    ): List<QueryBoardDTO>?

    fun selectBoardByBoardTag(
        @Param("boardTagList") boardTagList: List<String>,
        @Param("lastId") lastId: Long?,
        @Param("pageSize") pageSize: Int,
    ): List<QueryBoardDTO>?
}