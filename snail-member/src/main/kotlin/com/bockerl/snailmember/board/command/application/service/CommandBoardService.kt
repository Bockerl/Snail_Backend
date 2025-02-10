package com.bockerl.snailmember.board.command.application.service

import com.bockerl.snailmember.board.command.domain.aggregate.vo.request.CommandBoardRequestVO
import com.bockerl.snailmember.board.command.domain.aggregate.vo.request.CommandBoardUpdateRequestVO
import org.springframework.web.multipart.MultipartFile

interface CommandBoardService {
    fun createBoard(commandBoardRequestVO: CommandBoardRequestVO, files: List<MultipartFile>)

    fun updateBoard(commandBoardUpdateRequestVO: CommandBoardUpdateRequestVO, files: List<MultipartFile>)
}