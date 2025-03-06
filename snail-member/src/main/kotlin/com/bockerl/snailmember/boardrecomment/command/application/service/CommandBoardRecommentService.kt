package com.bockerl.snailmember.boardrecomment.command.application.service

import com.bockerl.snailmember.boardrecomment.command.application.dto.CommandBoardRecommentCreateByGifDTO
import com.bockerl.snailmember.boardrecomment.command.application.dto.CommandBoardRecommentCreateDTO
import com.bockerl.snailmember.boardrecomment.command.application.dto.CommandBoardRecommentDeleteDTO
import org.springframework.web.multipart.MultipartFile

interface CommandBoardRecommentService {
    fun createBoardRecomment(commandBoardRecommentCreateDTO: CommandBoardRecommentCreateDTO)

    fun createBoardRecommentByGif(
        commandBoardRecommentCreateByGifDTO: CommandBoardRecommentCreateByGifDTO,
        file: MultipartFile,
    )

    fun deleteBoardRecomment(commandBoardRecommentDeleteDTO: CommandBoardRecommentDeleteDTO)
}