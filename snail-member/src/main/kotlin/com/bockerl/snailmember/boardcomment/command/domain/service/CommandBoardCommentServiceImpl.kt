package com.bockerl.snailmember.boardcomment.command.domain.service

import com.bockerl.snailmember.boardcomment.command.application.dto.CommandBoardCommentCreateByGifDTO
import com.bockerl.snailmember.boardcomment.command.application.dto.CommandBoardCommentCreateDTO
import com.bockerl.snailmember.boardcomment.command.application.dto.CommandBoardCommentDeleteDTO
import com.bockerl.snailmember.boardcomment.command.application.service.CommandBoardCommentService
import com.bockerl.snailmember.boardcomment.command.domain.aggregate.entity.BoardComment
import com.bockerl.snailmember.boardcomment.command.domain.repository.CommandBoardCommentRepository
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.file.command.application.dto.CommandFileDTO
import com.bockerl.snailmember.file.command.application.service.CommandFileService
import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType
import jakarta.transaction.Transactional
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class CommandBoardCommentServiceImpl(
    private val commandBoardCommentRepository: CommandBoardCommentRepository,
    private val commandFileService: CommandFileService,
    private val redisTemplate: RedisTemplate<String, Any>,
) : CommandBoardCommentService {
    @Transactional
    override fun createBoardComment(commandBoardCommentCreateDTO: CommandBoardCommentCreateDTO) {
        // 따닥 방지
        if (redisTemplate.hasKey(commandBoardCommentCreateDTO.idempotencyKey)) {
            throw CommonException(ErrorCode.ALREADY_REQUESTED)
        }

        val boardComment =
            BoardComment(
                boardCommentContents = commandBoardCommentCreateDTO.boardCommentContents,
                boardId = extractDigits(commandBoardCommentCreateDTO.boardId),
                memberId = extractDigits(commandBoardCommentCreateDTO.memberId),
            )

        commandBoardCommentRepository.save(boardComment)
        // 설명. 데이터 변경시 해당하는 해당 게시글 댓글들 캐시 초기화
        redisTemplate.delete("boardComments:comment/${commandBoardCommentCreateDTO.boardId}")
        redisTemplate.delete("boardComments:member/${commandBoardCommentCreateDTO.memberId}")

        val idempotencyScript =
            """
            local res = redis.call("set", KEYS[1], ARGV[1], "EX", ARGV[2])
            return res
            """.trimIndent()

        val idempotencyRedisScript = DefaultRedisScript<String>(idempotencyScript, String::class.java)
        val ttlInSeconds = "3600" // 1시간 TTL

        val result =
            redisTemplate.execute(
                idempotencyRedisScript,
                listOf(commandBoardCommentCreateDTO.idempotencyKey),
                "PROCESSED",
                ttlInSeconds,
            )

        // result가 "OK"이면 SET 명령이 성공적으로 실행된 것입니다.
        if (result != "OK") {
            throw CommonException(ErrorCode.REDIS_ERROR)
        }
    }

    @Transactional
    override fun createBoardCommentByGif(
        commandBoardCommentCreateByGifDTO: CommandBoardCommentCreateByGifDTO,
        file: MultipartFile,
    ) {
        val boardComment =
            BoardComment(
                boardId = extractDigits(commandBoardCommentCreateByGifDTO.boardId),
                memberId = extractDigits(commandBoardCommentCreateByGifDTO.memberId),
            )

        val boardCommentEntity = commandBoardCommentRepository.save(boardComment)

        val commandFileDTO =
            boardCommentEntity.boardCommentId?.let {
                CommandFileDTO(
                    fileTargetType = FileTargetType.BOARD_COMMENT,
                    fileTargetId = formattedBoardCommentId(it),
                    memberId = commandBoardCommentCreateByGifDTO.memberId,
                    idempotencyKey = commandBoardCommentCreateByGifDTO.idempotencyKey,
                )
            }

        commandFileDTO?.let { commandFileService.createSingleFile(file, commandFileDTO) }

        redisTemplate.delete("boardComments:comment/${commandBoardCommentCreateByGifDTO.boardId}")
        redisTemplate.delete("boardComments:member/${commandBoardCommentCreateByGifDTO.memberId}")

        val idempotencyScript =
            """
            local res = redis.call("set", KEYS[1], ARGV[1], "EX", ARGV[2])
            return res
            """.trimIndent()

        val idempotencyRedisScript = DefaultRedisScript<String>(idempotencyScript, String::class.java)
        val ttlInSeconds = "3600" // 1시간 TTL

        val result =
            redisTemplate.execute(
                idempotencyRedisScript,
                listOf(commandBoardCommentCreateByGifDTO.idempotencyKey),
                "PROCESSED",
                ttlInSeconds,
            )

        // result가 "OK"이면 SET 명령이 성공적으로 실행된 것입니다.
        if (result != "OK") {
            throw CommonException(ErrorCode.REDIS_ERROR)
        }
    }

    @Transactional
    override fun deleteBoardComment(commandBoardCommentDeleteDTO: CommandBoardCommentDeleteDTO) {
        // 따닥 방지
        if (redisTemplate.hasKey(commandBoardCommentDeleteDTO.idempotencyKey)) {
            throw CommonException(ErrorCode.ALREADY_REQUESTED)
        }

        val boardCommentId = extractDigits(commandBoardCommentDeleteDTO.boardCommentId)
        val boardComment =
            commandBoardCommentRepository
                .findById(
                    boardCommentId,
                ).orElseThrow { CommonException(ErrorCode.NOT_FOUND_BOARD_COMMENT) }

        boardComment.apply {
            active = false
        }

        // 설명. 내용이 비어있을 때, 파일 지우러 감
        boardComment.boardCommentContents ?: run {
            val commandFileDTO =
                CommandFileDTO(
                    fileTargetType = FileTargetType.BOARD_COMMENT,
                    fileTargetId = formattedBoardCommentId(boardCommentId),
                    memberId = commandBoardCommentDeleteDTO.memberId,
                    idempotencyKey = commandBoardCommentDeleteDTO.idempotencyKey,
                )
            commandFileService.deleteFile(commandFileDTO)
        }

        redisTemplate.delete("boardComments:comment/${commandBoardCommentDeleteDTO.boardId}")
        redisTemplate.delete("boardComments:member/${commandBoardCommentDeleteDTO.memberId}")

        val idempotencyScript =
            """
            local res = redis.call("set", KEYS[1], ARGV[1], "EX", ARGV[2])
            return res
            """.trimIndent()

        val idempotencyRedisScript = DefaultRedisScript<String>(idempotencyScript, String::class.java)
        val ttlInSeconds = "3600" // 1시간 TTL

        val result =
            redisTemplate.execute(
                idempotencyRedisScript,
                listOf(commandBoardCommentDeleteDTO.idempotencyKey),
                "PROCESSED",
                ttlInSeconds,
            )

        // result가 "OK"이면 SET 명령이 성공적으로 실행된 것입니다.
        if (result != "OK") {
            throw CommonException(ErrorCode.REDIS_ERROR)
        }
    }

    fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()

    fun formattedBoardCommentId(boardId: Long): String = "BOA-COM-${boardId.toString().padStart(8, '0') ?: "00000000"}"
}