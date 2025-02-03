package com.bockerl.snailmember.board.query.service

import com.bockerl.snailmember.board.command.application.dto.BoardDTO

interface QueryBoardService{
    fun readBoardByBoardId(boardId: Long): BoardDTO

}