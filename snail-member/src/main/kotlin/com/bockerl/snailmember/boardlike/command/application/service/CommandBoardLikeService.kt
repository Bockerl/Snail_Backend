package com.bockerl.snailmember.boardlike.command.application.service

import com.bockerl.snailmember.boardlike.command.application.dto.CommandBoardLikeDTO

interface CommandBoardLikeService {
    fun createBoardLike(commandBoardLikeDTO: CommandBoardLikeDTO)

    fun createBoardLikeEventList(boardLikeList: List<CommandBoardLikeDTO>)

    fun deleteBoardLike(commandBoardLikeDTO: CommandBoardLikeDTO)

    fun deleteBoardLikeEvent(commandBoardLikeDTO: CommandBoardLikeDTO)

//    fun readBoardLike(boardId: String): List<CommandBoardLikeMemberIdsResponseVO>
//
//    fun readBoardLikeCount(boardId: String): Long
//
//    fun readBoardIdsByMemberId(memberId: String): List<QueryBoardResponseVO>
}