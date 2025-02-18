package com.bockerl.snailmember.boardcommentlike.command.application.service

import com.bockerl.snailmember.boardcommentlike.command.domain.vo.request.CommandBoardCommentLikeRequestVO
import com.bockerl.snailmember.boardcommentlike.command.domain.vo.response.CommandBoardCommentLikeMemberIdsResponseVO

interface CommandBoardCommentLikeService {
    fun createBoardCommentLike(commandBoardCommentLikeRequestVO: CommandBoardCommentLikeRequestVO)

    fun deleteBoardCommentLike(commandBoardCommentLikeRequestVO: CommandBoardCommentLikeRequestVO)

    fun readBoardCommentLike(boardCommentId: String): List<CommandBoardCommentLikeMemberIdsResponseVO>

    fun readBoardCommentLikeCount(boardCommentId: String): Long
}