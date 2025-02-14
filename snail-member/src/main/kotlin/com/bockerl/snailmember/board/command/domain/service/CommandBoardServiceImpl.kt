/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */

package com.bockerl.snailmember.board.command.domain.service

import com.bockerl.snailmember.board.command.application.service.CommandBoardService
import com.bockerl.snailmember.board.command.domain.aggregate.entity.Board
import com.bockerl.snailmember.board.command.domain.aggregate.vo.request.CommandBoardCreateRequestVO
import com.bockerl.snailmember.board.command.domain.aggregate.vo.request.CommandBoardDeleteRequestVO
import com.bockerl.snailmember.board.command.domain.aggregate.vo.request.CommandBoardUpdateRequestVO
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
        commandBoardCreateRequestVO: CommandBoardCreateRequestVO,
        files: List<MultipartFile>,
    ) {
        val board =
            Board(
                boardContents = commandBoardCreateRequestVO.boardContents,
                boardType = commandBoardCreateRequestVO.boardType,
                boardTag = commandBoardCreateRequestVO.boardTag,
                boardLocation = commandBoardCreateRequestVO.boardLocation,
                boardAccessLevel = commandBoardCreateRequestVO.boardAccessLevel,
                memberId = commandBoardCreateRequestVO.memberId,
            )

        val boardEntity = commandBoardRepository.save(board)

        if (files.isNotEmpty()) {
            val commandFileRequestVO =
                boardEntity.boardId?.let {
                    CommandFileRequestVO(
                        fileTargetType = FileTargetType.BOARD,
                        fileTargetId = it,
                        memberId = commandBoardCreateRequestVO.memberId,
                    )
                }

            commandFileRequestVO?.let { commandFileService.uploadFiles(files, it) }
        }
    }

    override fun updateBoard(
        commandBoardUpdateRequestVO: CommandBoardUpdateRequestVO,
        files: List<MultipartFile>,
    ) {
        val boardId = extractDigits(commandBoardUpdateRequestVO.boardId)

        val board = commandBoardRepository.findById(boardId).orElseThrow { CommonException(ErrorCode.NOT_FOUND_BOARD) }

        board.apply {
            boardContents = commandBoardUpdateRequestVO.boardContents
            boardType = commandBoardUpdateRequestVO.boardType
            boardTag = commandBoardUpdateRequestVO.boardTag
            boardLocation = commandBoardUpdateRequestVO.boardLocation
            boardAccessLevel = commandBoardUpdateRequestVO.boardAccessLevel
            memberId = commandBoardUpdateRequestVO.memberId
        }

        val boardEntity = commandBoardRepository.save(board)

        if (files.isNotEmpty()) {
            val commandFileRequestVO =
                boardEntity.boardId?.let {
                    CommandFileRequestVO(
                        fileTargetType = FileTargetType.BOARD,
                        fileTargetId = it,
                        memberId = commandBoardUpdateRequestVO.memberId,
                    )
                }

            commandFileRequestVO?.let { commandFileService.updateFiles(it, commandBoardUpdateRequestVO.deleteFilesIds, files) }
        }
    }

    // 설명. soft delete로 바꾸기
    override fun deleteBoard(commandBoardDeleteRequestVO: CommandBoardDeleteRequestVO) {
        val boardId = extractDigits(commandBoardDeleteRequestVO.boardId)
        val board = commandBoardRepository.findById(boardId).orElseThrow { CommonException(ErrorCode.NOT_FOUND_BOARD) }

        board.apply {
            active = false
        }

        val commandFileRequestVO =
            CommandFileRequestVO(
                fileTargetType = FileTargetType.BOARD,
                fileTargetId = boardId,
                memberId = commandBoardDeleteRequestVO.memberId,
            )

        commandFileService.deleteFile(commandFileRequestVO)
    }

    fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()
}