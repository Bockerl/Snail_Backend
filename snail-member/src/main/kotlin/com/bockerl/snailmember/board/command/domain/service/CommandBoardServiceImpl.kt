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
import com.bockerl.snailmember.board.command.domain.aggregate.enums.BoardTag
import com.bockerl.snailmember.board.command.domain.repository.CommandBoardRepository
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.file.command.application.dto.CommandFileDTO
import com.bockerl.snailmember.file.command.application.service.CommandFileService
import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType
import jakarta.transaction.Transactional
import org.springframework.data.redis.core.Cursor
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class CommandBoardServiceImpl(
    private val commandBoardRepository: CommandBoardRepository,
    private val commandFileService: CommandFileService,
    private val redisTemplate: RedisTemplate<String, Any>,
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
            val commandFileDTO =
                boardEntity.boardId?.let {
                    CommandFileDTO(
                        fileTargetType = FileTargetType.BOARD,
                        fileTargetId = formattedBoardId(it),
                        memberId = commandBoardCreateDTO.memberId,
                    )
                }

            commandFileDTO?.let { commandFileService.createFiles(files, it) }
        }

        redisTemplate.delete("board/${commandBoardCreateDTO.boardTag}")
        invalidateByTag(board.boardTag)
        redisTemplate.delete("board/${commandBoardCreateDTO.boardType}")
    }

    @Transactional
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
            val commandFileDTO =
                boardEntity.boardId?.let {
                    CommandFileDTO(
                        fileTargetType = FileTargetType.BOARD,
                        fileTargetId = formattedBoardId(it),
                        memberId = commandBoardUpdateDTO.memberId,
                    )
                }

            commandFileDTO?.let { commandFileService.updateFiles(it, commandBoardUpdateDTO.deleteFilesIds, files) }
        }

        redisTemplate.delete("board/${commandBoardUpdateDTO.boardTag}")
        invalidateByTag(board.boardTag)
        redisTemplate.delete("board/${commandBoardUpdateDTO.boardType}")
    }

    @Transactional
    override fun deleteBoard(commandBoardDeleteDTO: CommandBoardDeleteDTO) {
        val boardId = extractDigits(commandBoardDeleteDTO.boardId)
        val board = commandBoardRepository.findById(boardId).orElseThrow { CommonException(ErrorCode.NOT_FOUND_BOARD) }

        board.apply {
            active = false
        }

        val commandFileDTO =
            CommandFileDTO(
                fileTargetType = FileTargetType.BOARD,
                fileTargetId = formattedBoardId(boardId),
                memberId = commandBoardDeleteDTO.memberId,
            )

        commandFileService.deleteFile(commandFileDTO)

        redisTemplate.delete("board/${board.boardTag}")
        invalidateByTag(board.boardTag)
        redisTemplate.delete("board/${board.boardType}")
    }

    private fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()

    private fun formattedBoardId(boardId: Long): String = "BOA-${boardId.toString().padStart(8, '0') ?: "00000000"}"

    // 설명. 점진적으로 커서를 이용해 키를 검색한다. (현재는 1000개씩 scan)
    private fun scanAndDelete(pattern: String) {
        // ScanOptions를 통해 매칭 패턴과 count(한번에 조회할 키 수)를 설정합니다.
        val scanOptions =
            ScanOptions
                .scanOptions()
                .match(pattern)
                .count(1000)
                .build()
        // SCAN 커서를 엽니다.
        val cursor: Cursor<String> = redisTemplate.scan(scanOptions)
        cursor.use {
            while (it.hasNext()) {
                val key = it.next()
                redisTemplate.delete(key)
            }
        }
    }

    private fun invalidateByTag(tag: BoardTag) {
        val pattern = "board/*$tag*"
        scanAndDelete(pattern)
    }
}