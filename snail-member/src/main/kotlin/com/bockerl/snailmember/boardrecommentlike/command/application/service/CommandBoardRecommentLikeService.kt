package com.bockerl.snailmember.boardrecommentlike.command.application.service

import com.bockerl.snailmember.boardrecommentlike.command.application.dto.CommandBoardRecommentLikeDTO

interface CommandBoardRecommentLikeService {
    fun createBoardRecommentLike(commandBoardRecommentLikeDTO: CommandBoardRecommentLikeDTO)

    fun createBoardRecommentLikeEventList(boardRecommentLikeList: List<CommandBoardRecommentLikeDTO>)

    fun deleteBoardRecommentLike(commandBoardRecommentLikeDTO: CommandBoardRecommentLikeDTO)

    fun deleteBoardRecommentLikeEvent(commandBoardRecommentLikeDTO: CommandBoardRecommentLikeDTO)
}