/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */

package com.bockerl.snailmember.board.command.domain.service

import com.bockerl.snailmember.board.command.application.dto.CommandBoardCreateDTO
import com.bockerl.snailmember.board.command.application.dto.CommandBoardDeleteDTO
import com.bockerl.snailmember.board.command.application.dto.CommandBoardUpdateDTO
import com.bockerl.snailmember.board.command.application.service.CommandBoardService
import com.bockerl.snailmember.board.command.domain.aggregate.entity.Board
import com.bockerl.snailmember.board.command.domain.repository.CommandBoardRepository
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.file.command.application.service.CommandFileService
import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType
import com.bockerl.snailmember.file.command.domain.aggregate.vo.CommandFileRequestVO
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class CommandBoardServiceImpl(
    private val commandBoardRepository: CommandBoardRepository,
    private val commandFileService: CommandFileService,
) : CommandBoardService {
    @Transactional
    override fun createBoard(
        commandBoardCreateDTO: CommandBoardCreateDTO,
        files: List<MultipartFile>,
    ) {
        val board =
            Board(
                boardContents = commandBoardCreateDTO.boardContents,
                boardType = commandBoardCreateDTO.boardType,
                boardTag = commandBoardCreateDTO.boardTag,
                boardLocation = commandBoardCreateDTO.boardLocation,
                boardAccessLevel = commandBoardCreateDTO.boardAccessLevel,
                memberId = extractDigits(commandBoardCreateDTO.memberId),
            )

        val boardEntity = commandBoardRepository.save(board)

        if (files.isNotEmpty()) {
            val commandFileRequestVO =
                boardEntity.boardId?.let {
                    CommandFileRequestVO(
                        fileTargetType = FileTargetType.BOARD,
                        fileTargetId = formattedBoardId(it),
                        memberId = commandBoardCreateDTO.memberId,
                    )
                }

            commandFileRequestVO?.let { commandFileService.uploadFiles(files, it) }
        }
    }

    override fun updateBoard(
        commandBoardUpdateDTO: CommandBoardUpdateDTO,
        files: List<MultipartFile>,
    ) {
        val boardId = extractDigits(commandBoardUpdateDTO.boardId)

        val board = commandBoardRepository.findById(boardId).orElseThrow { CommonException(ErrorCode.NOT_FOUND_BOARD) }

        board.apply {
            boardContents = commandBoardUpdateDTO.boardContents
            boardType = commandBoardUpdateDTO.boardType
            boardTag = commandBoardUpdateDTO.boardTag
            boardLocation = commandBoardUpdateDTO.boardLocation
            boardAccessLevel = commandBoardUpdateDTO.boardAccessLevel
            memberId = extractDigits(commandBoardUpdateDTO.memberId)
        }

        val boardEntity = commandBoardRepository.save(board)

        if (files.isNotEmpty()) {
            val commandFileRequestVO =
                boardEntity.boardId?.let {
                    CommandFileRequestVO(
                        fileTargetType = FileTargetType.BOARD,
                        fileTargetId = formattedBoardId(it),
                        memberId = commandBoardUpdateDTO.memberId,
                    )
                }

            commandFileRequestVO?.let { commandFileService.updateFiles(it, commandBoardUpdateDTO.deleteFilesIds, files) }
        }
    }

    // 설명. soft delete로 바꾸기
    override fun deleteBoard(commandBoardDeleteDTO: CommandBoardDeleteDTO) {
        val boardId = extractDigits(commandBoardDeleteDTO.boardId)
        val board = commandBoardRepository.findById(boardId).orElseThrow { CommonException(ErrorCode.NOT_FOUND_BOARD) }

        board.apply {
            active = false
        }

        val commandFileRequestVO =
            CommandFileRequestVO(
                fileTargetType = FileTargetType.BOARD,
                fileTargetId = formattedBoardId(boardId),
                memberId = commandBoardDeleteDTO.memberId,
            )

        commandFileService.deleteFile(commandFileRequestVO)
    }

    fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()

    fun formattedBoardId(boardId: Long): String = "BOA-${boardId.toString().padStart(8, '0') ?: "00000000"}"
}