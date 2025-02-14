package com.bockerl.snailmember.boardlike.command.application.service

import com.bockerl.snailmember.boardlike.command.domain.vo.request.CommandBoardLikeCreateRequestVO

interface CommandBoardLikeService {
    fun createBoardLike(commandBoardLikeCreateRequestVO: CommandBoardLikeCreateRequestVO)
}