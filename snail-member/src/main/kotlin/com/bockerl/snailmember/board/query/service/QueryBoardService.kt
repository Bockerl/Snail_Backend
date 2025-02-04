package com.bockerl.snailmember.board.query.service

import com.bockerl.snailmember.board.command.application.dto.BoardDTO
import com.bockerl.snailmember.board.command.domain.aggregate.vo.response.BoardResponseVO

interface QueryBoardService{
    fun readBoardByBoardId(boardId: Long): BoardDTO

    fun readBoardByBoardType(boardType: String): List<BoardDTO>

    fun readBoardByBoardTag(boardTagList: List<String>): List<BoardDTO>

}