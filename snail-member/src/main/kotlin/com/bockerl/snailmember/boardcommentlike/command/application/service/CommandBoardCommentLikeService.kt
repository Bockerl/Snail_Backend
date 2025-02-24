package com.bockerl.snailmember.boardcommentlike.command.application.service

import com.bockerl.snailmember.boardcommentlike.command.application.dto.CommandBoardCommentLikeDTO
import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.entity.BoardCommentLike
import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.vo.response.CommandBoardCommentLikeMemberIdsResponseVO

interface CommandBoardCommentLikeService {
    fun createBoardCommentLike(commandBoardCommentLikeDTO: CommandBoardCommentLikeDTO)

    fun createBoardCommentLikeEventList(boardCommentLikeList: List<BoardCommentLike>)

    fun deleteBoardCommentLike(commandBoardCommentLikeDTO: CommandBoardCommentLikeDTO)

    fun deleteBoardCommentLikeEvent(boardCommentLike: BoardCommentLike)

    fun readBoardCommentLike(boardCommentId: String): List<CommandBoardCommentLikeMemberIdsResponseVO>

    fun readBoardCommentLikeCount(boardCommentId: String): Long
}