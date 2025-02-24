package com.bockerl.snailmember.boardlike.command.application.service

import com.bockerl.snailmember.board.query.vo.QueryBoardResponseVO
import com.bockerl.snailmember.boardlike.command.application.dto.CommandBoardLikeDTO
import com.bockerl.snailmember.boardlike.command.domain.aggregate.entity.BoardLike
import com.bockerl.snailmember.boardlike.command.domain.aggregate.vo.response.CommandBoardLikeMemberIdsResponseVO

interface CommandBoardLikeService {
    fun createBoardLike(commandBoardLikeDTO: CommandBoardLikeDTO)

    fun createBoardLikeEventList(boardLikeList: List<BoardLike>)

    fun deleteBoardLike(commandBoardLikeDTO: CommandBoardLikeDTO)

    fun deleteBoardLikeEvent(boardLike: BoardLike)

    fun readBoardLike(boardId: String): List<CommandBoardLikeMemberIdsResponseVO>

    fun readBoardLikeCount(boardId: String): Long

    fun readBoardIdsByMemberId(memberId: String): List<QueryBoardResponseVO>
}