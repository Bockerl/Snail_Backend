package com.bockerl.snailmember.boardcommentlike.command.application.service

import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.entity.BoardCommentLike
import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.vo.request.CommandBoardCommentLikeRequestVO
import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.vo.response.CommandBoardCommentLikeMemberIdsResponseVO

interface CommandBoardCommentLikeService {
    fun createBoardCommentLike(commandBoardCommentLikeRequestVO: CommandBoardCommentLikeRequestVO)

    fun createBoardCommentLikeEventList(boardCommentLikeList: List<BoardCommentLike>)

    fun deleteBoardCommentLike(commandBoardCommentLikeRequestVO: CommandBoardCommentLikeRequestVO)

    fun deleteBoardCommentLikeEvent(boardCommentLike: BoardCommentLike)

    fun readBoardCommentLike(boardCommentId: String): List<CommandBoardCommentLikeMemberIdsResponseVO>

    fun readBoardCommentLikeCount(boardCommentId: String): Long
}