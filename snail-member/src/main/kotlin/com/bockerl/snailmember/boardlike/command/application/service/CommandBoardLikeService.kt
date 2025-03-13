package com.bockerl.snailmember.boardlike.command.application.service

import com.bockerl.snailmember.boardlike.command.application.dto.CommandBoardLikeDTO
import com.bockerl.snailmember.boardlike.command.application.dto.CommandBoardLikeEventDTO

interface CommandBoardLikeService {
    fun createBoardLike(commandBoardLikeDTO: CommandBoardLikeDTO)

    fun createBoardLikeEventList(boardLikeList: List<CommandBoardLikeEventDTO>)

    fun deleteBoardLike(commandBoardLikeDTO: CommandBoardLikeDTO)

    fun deleteBoardLikeEvent(commandBoardLikeEventDTO: CommandBoardLikeEventDTO)

//    fun readBoardLike(boardId: String): List<CommandBoardLikeMemberIdsResponseVO>
//
//    fun readBoardLikeCount(boardId: String): Long
//
//    fun readBoardIdsByMemberId(memberId: String): List<QueryBoardResponseVO>
}