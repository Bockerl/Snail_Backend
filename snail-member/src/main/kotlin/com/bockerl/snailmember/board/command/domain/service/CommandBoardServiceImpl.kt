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
import org.springframework.data.redis.core.script.DefaultRedisScript
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
        // 따닥 방지
        if (redisTemplate.hasKey(commandBoardCreateDTO.idempotencyKey)) {
            throw CommonException(ErrorCode.ALREADY_REQUESTED)
        }

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
                        idempotencyKey = commandBoardCreateDTO.idempotencyKey,
                    )
                }

            commandFileDTO?.let { commandFileService.createFiles(files, it) }
        }

        redisTemplate.delete("board/${commandBoardCreateDTO.boardTag}")
        invalidateByTag(board.boardTag)
        redisTemplate.delete("board/${commandBoardCreateDTO.boardType}")

        val idempotencyScript =
            """
            local res = redis.call("set", KEYS[1], ARGV[1], "EX", ARGV[2])
            return res
            """.trimIndent()

        val idempotencyRedisScript = DefaultRedisScript<String>(idempotencyScript, String::class.java)
        val ttlInSeconds = "3600" // 1시간 TTL

        val result = redisTemplate.execute(idempotencyRedisScript, listOf(commandBoardCreateDTO.idempotencyKey), "PROCESSED", ttlInSeconds)

        // result가 "OK"이면 SET 명령이 성공적으로 실행된 것입니다.
        if (result != "OK") {
            throw CommonException(ErrorCode.REDIS_ERROR)
        }
    }

    @Transactional
    override fun updateBoard(
        commandBoardUpdateDTO: CommandBoardUpdateDTO,
        files: List<MultipartFile>,
    ) {
        // 따닥 방지
        if (redisTemplate.hasKey(commandBoardUpdateDTO.idempotencyKey)) {
            throw CommonException(ErrorCode.ALREADY_REQUESTED)
        }

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
                        idempotencyKey = commandBoardUpdateDTO.idempotencyKey,
                    )
                }

            commandFileDTO?.let { commandFileService.updateFiles(it, commandBoardUpdateDTO.deleteFilesIds, files) }
        }

        redisTemplate.delete("board/${commandBoardUpdateDTO.boardTag}")
        invalidateByTag(board.boardTag)
        redisTemplate.delete("board/${commandBoardUpdateDTO.boardType}")

        val idempotencyScript =
            """
            local res = redis.call("set", KEYS[1], ARGV[1], "EX", ARGV[2])
            return res
            """.trimIndent()

        val idempotencyRedisScript = DefaultRedisScript<String>(idempotencyScript, String::class.java)
        val ttlInSeconds = "3600" // 1시간 TTL

        val result = redisTemplate.execute(idempotencyRedisScript, listOf(commandBoardUpdateDTO.idempotencyKey), "PROCESSED", ttlInSeconds)

        // result가 "OK"이면 SET 명령이 성공적으로 실행된 것입니다.
        if (result != "OK") {
            throw CommonException(ErrorCode.REDIS_ERROR)
        }
    }

    @Transactional
    override fun deleteBoard(commandBoardDeleteDTO: CommandBoardDeleteDTO) {
        // 따닥 방지
        if (redisTemplate.hasKey(commandBoardDeleteDTO.idempotencyKey)) {
            throw CommonException(ErrorCode.ALREADY_REQUESTED)
        }

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
                idempotencyKey = commandBoardDeleteDTO.idempotencyKey,
            )

        commandFileService.deleteFile(commandFileDTO)

        redisTemplate.delete("board/${board.boardTag}")
        invalidateByTag(board.boardTag)
        redisTemplate.delete("board/${board.boardType}")

        val idempotencyScript =
            """
            local res = redis.call("set", KEYS[1], ARGV[1], "EX", ARGV[2])
            return res
            """.trimIndent()

        val idempotencyRedisScript = DefaultRedisScript<String>(idempotencyScript, String::class.java)
        val ttlInSeconds = "3600" // 1시간 TTL

        val result = redisTemplate.execute(idempotencyRedisScript, listOf(commandBoardDeleteDTO.idempotencyKey), "PROCESSED", ttlInSeconds)

        // result가 "OK"이면 SET 명령이 성공적으로 실행된 것입니다.
        if (result != "OK") {
            throw CommonException(ErrorCode.REDIS_ERROR)
        }
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