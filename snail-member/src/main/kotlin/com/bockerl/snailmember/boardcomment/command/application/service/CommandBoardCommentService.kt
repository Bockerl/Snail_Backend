package com.bockerl.snailmember.boardcomment.command.application.service

import com.bockerl.snailmember.boardcomment.command.domain.aggregate.vo.request.CommandBoardCommentCreateByGifRequestVO
import com.bockerl.snailmember.boardcomment.command.domain.aggregate.vo.request.CommandBoardCommentCreateRequestVO
import com.bockerl.snailmember.boardcomment.command.domain.aggregate.vo.request.CommandBoardCommentDeleteRequestVO
import org.springframework.web.multipart.MultipartFile

interface CommandBoardCommentService {
    fun createBoardComment(commandBoardCommentCreateRequestVO: CommandBoardCommentCreateRequestVO)

    fun createBoardCommentByGif(
        commandBoardCommentCreateByGifRequestVO: CommandBoardCommentCreateByGifRequestVO,
        file: MultipartFile,
    )

    fun deleteBoardComment(commandBoardCommentDeleteRequestVO: CommandBoardCommentDeleteRequestVO)
}