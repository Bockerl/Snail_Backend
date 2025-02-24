package com.bockerl.snailmember.boardcomment.command.application.service

import com.bockerl.snailmember.boardcomment.command.application.dto.CommandBoardCommentCreateByGifDTO
import com.bockerl.snailmember.boardcomment.command.application.dto.CommandBoardCommentCreateDTO
import com.bockerl.snailmember.boardcomment.command.application.dto.CommandBoardCommentDeleteDTO
import org.springframework.web.multipart.MultipartFile

interface CommandBoardCommentService {
    fun createBoardComment(commandBoardCommentCreateDTO: CommandBoardCommentCreateDTO)

    fun createBoardCommentByGif(
        commandBoardCommentCreateByGifDTO: CommandBoardCommentCreateByGifDTO,
        file: MultipartFile,
    )

    fun deleteBoardComment(commandBoardCommentDeleteDTO: CommandBoardCommentDeleteDTO)
}