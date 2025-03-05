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
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class CommandBoardCommentServiceImpl(
    private val commandBoardCommentRepository: CommandBoardCommentRepository,
    private val commandFileService: CommandFileService,
    private val cacheManager: RedisCacheManager,
) : CommandBoardCommentService {
    @Transactional
    override fun createBoardComment(commandBoardCommentCreateDTO: CommandBoardCommentCreateDTO) {
        val boardComment =
            BoardComment(
                boardCommentContents = commandBoardCommentCreateDTO.boardCommentContents,
                boardId = extractDigits(commandBoardCommentCreateDTO.boardId),
                memberId = extractDigits(commandBoardCommentCreateDTO.memberId),
            )

        commandBoardCommentRepository.save(boardComment)
        // 설명. 데이터 변경시 해당하는 해당 게시글 댓글들 캐시 초기화
        cacheManager.getCache("boardComments:comment/${commandBoardCommentCreateDTO.boardId}")?.clear()
        cacheManager.getCache("boardComments:member/${commandBoardCommentCreateDTO.memberId}")?.clear()
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
                )
            }

        commandFileDTO?.let { commandFileService.createSingleFile(file, commandFileDTO) }

        cacheManager.getCache("boardComments:comment/${commandBoardCommentCreateByGifDTO.boardId}")?.clear()
        cacheManager.getCache("boardComments:member/${commandBoardCommentCreateByGifDTO.memberId}")?.clear()
    }

    @Transactional
//    @CacheEvict(value = ["/boardComment/{boardComment.boardId}"], allEntries = true)
    override fun deleteBoardComment(commandBoardCommentDeleteDTO: CommandBoardCommentDeleteDTO) {
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
        boardComment.boardCommentContents?.let {
            val commandFileDTO =
                CommandFileDTO(
                    fileTargetType = FileTargetType.BOARD_COMMENT,
                    fileTargetId = formattedBoardCommentId(boardCommentId),
                    memberId = commandBoardCommentDeleteDTO.memberId,
                )
            commandFileService.deleteFile(commandFileDTO)
        }

        cacheManager.getCache("boardComments:comment/${commandBoardCommentDeleteDTO.boardId}")?.clear()
        cacheManager.getCache("boardComments:member/${commandBoardCommentDeleteDTO.memberId}")?.clear()
    }

    fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()

    fun formattedBoardCommentId(boardId: Long): String = "BOA-COM-${boardId.toString().padStart(8, '0') ?: "00000000"}"
}