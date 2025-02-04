package com.bockerl.snailmember.board.query.repository

import com.bockerl.snailmember.board.command.application.dto.BoardDTO
import com.bockerl.snailmember.board.command.domain.aggregate.entity.Board
import org.apache.ibatis.annotations.Mapper

@Mapper
interface BoardMapper {
    /* 설명. boardDTO에는 long값 받는건 어떤지? */
    fun selectBoardByBoardId(boardId: Long): Board?

    fun selectBoardByBoardType(boardType: String): List<Board>?
}