package com.bockerl.snailmember.board.command.application.service

import com.bockerl.snailmember.board.command.domain.aggregate.vo.request.CommandBoardCreateRequestVO
import com.bockerl.snailmember.board.command.domain.aggregate.vo.request.CommandBoardDeleteRequestVO
import com.bockerl.snailmember.board.command.domain.aggregate.vo.request.CommandBoardUpdateRequestVO
import org.springframework.web.multipart.MultipartFile

interface CommandBoardService {
    fun createBoard(commandBoardCreateRequestVO: CommandBoardCreateRequestVO, files: List<MultipartFile>)

    fun updateBoard(commandBoardUpdateRequestVO: CommandBoardUpdateRequestVO, files: List<MultipartFile>)

    fun deleteBoard(commandBoardDeleteRequestVO: CommandBoardDeleteRequestVO)
}