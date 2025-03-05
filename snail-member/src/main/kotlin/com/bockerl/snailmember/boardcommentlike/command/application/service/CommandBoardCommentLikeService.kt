package com.bockerl.snailmember.boardcommentlike.command.application.service

import com.bockerl.snailmember.boardcommentlike.command.application.dto.CommandBoardCommentLikeDTO

interface CommandBoardCommentLikeService {
    fun createBoardCommentLike(commandBoardCommentLikeDTO: CommandBoardCommentLikeDTO)

    fun createBoardCommentLikeEventList(boardCommentLikeList: List<CommandBoardCommentLikeDTO>)

    fun deleteBoardCommentLike(commandBoardCommentLikeDTO: CommandBoardCommentLikeDTO)

    fun deleteBoardCommentLikeEvent(commandBoardCommentLikeDTO: CommandBoardCommentLikeDTO)

//    fun readBoardCommentLike(boardCommentId: String): List<CommandBoardCommentLikeMemberIdsResponseVO>
//
//    fun readBoardCommentLikeCount(boardCommentId: String): Long
}