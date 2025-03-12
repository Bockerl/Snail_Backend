/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */

package com.bockerl.snailmember.board.command.application.service

import com.bockerl.snailmember.board.command.application.dto.CommandBoardCreateDTO
import com.bockerl.snailmember.board.command.application.dto.CommandBoardDeleteDTO
import com.bockerl.snailmember.board.command.application.dto.CommandBoardUpdateDTO
import org.springframework.web.multipart.MultipartFile

interface CommandBoardService {
    fun createBoard(
        commandBoardCreateDTO: CommandBoardCreateDTO,
        files: List<MultipartFile>,
    )

    fun updateBoard(
        commandBoardUpdateDTO: CommandBoardUpdateDTO,
        files: List<MultipartFile>,
    )

    fun deleteBoard(commandBoardDeleteDTO: CommandBoardDeleteDTO)
}