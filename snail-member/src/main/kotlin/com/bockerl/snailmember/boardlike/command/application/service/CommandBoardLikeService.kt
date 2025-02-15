package com.bockerl.snailmember.boardlike.command.application.service

import com.bockerl.snailmember.board.query.vo.QueryBoardResponseVO
import com.bockerl.snailmember.boardlike.command.domain.vo.request.CommandBoardLikeRequestVO
import com.bockerl.snailmember.boardlike.command.domain.vo.response.CommandBoardLikeMemberIdsResponseVO

interface CommandBoardLikeService {
    fun createBoardLike(commandBoardLikeRequestVO: CommandBoardLikeRequestVO)

    fun deleteBoardLike(commandBoardLikeRequestVO: CommandBoardLikeRequestVO)

    fun readBoardLike(boardId: String): List<CommandBoardLikeMemberIdsResponseVO>

    fun readBoardLikeCount(boardId: String): Long

    fun readBoardIdsByMemberId(memberId: String): List<QueryBoardResponseVO>
}