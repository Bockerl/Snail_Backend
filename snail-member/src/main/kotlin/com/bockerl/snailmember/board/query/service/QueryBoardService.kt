package com.bockerl.snailmember.board.query.service

import com.bockerl.snailmember.board.query.vo.QueryBoardResponseVO

interface QueryBoardService {
    fun readBoardByBoardId(boardId: Long): QueryBoardResponseVO

    fun readBoardByBoardType(boardType: String): List<QueryBoardResponseVO>

    fun readBoardByBoardTag(boardTagList: List<String>): List<QueryBoardResponseVO>
}
