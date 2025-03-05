package com.bockerl.snailmember.boardrecomment.command.domain.service

import com.bockerl.snailmember.boardrecomment.command.application.dto.CommandBoardRecommentCreateByGifDTO
import com.bockerl.snailmember.boardrecomment.command.application.dto.CommandBoardRecommentCreateDTO
import com.bockerl.snailmember.boardrecomment.command.application.dto.CommandBoardRecommentDeleteDTO
import com.bockerl.snailmember.boardrecomment.command.application.service.CommandBoardRecommentService
import com.bockerl.snailmember.boardrecomment.command.domain.aggregate.entity.BoardRecomment
import com.bockerl.snailmember.boardrecomment.command.domain.repository.CommandBoardRecommentRepository
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
class CommandBoardRecommentServiceImpl(
    private val commandBoardRecommentRepository: CommandBoardRecommentRepository,
    private val commandFileService: CommandFileService,
    private val cacheManager: RedisCacheManager,
) : CommandBoardRecommentService {
    @Transactional
    override fun createBoardRecomment(commandBoardRecommentCreateDTO: CommandBoardRecommentCreateDTO) {
        val boardRecomment =
            BoardRecomment(
                boardRecommentContents = commandBoardRecommentCreateDTO.boardCommentContents,
                boardId = extractDigits(commandBoardRecommentCreateDTO.boardId),
                memberId = extractDigits(commandBoardRecommentCreateDTO.memberId),
                boardCommentId = extractDigits(commandBoardRecommentCreateDTO.boardCommentId),
            )

        commandBoardRecommentRepository.save(boardRecomment)
        // 설명. 데이터 변경시 해당하는 해당 게시글 댓글들 캐시 초기화
        cacheManager.getCache("boardRecomments/${commandBoardRecommentCreateDTO.boardCommentId}")?.clear()
        cacheManager.getCache("boardRecomments/${commandBoardRecommentCreateDTO.memberId}")?.clear()
    }

    @Transactional
    override fun createBoardRecommentByGif(
        commandBoardRecommentCreateByGifDTO: CommandBoardRecommentCreateByGifDTO,
        file: MultipartFile,
    ) {
        val boardRecomment =
            BoardRecomment(
                boardId = extractDigits(commandBoardRecommentCreateByGifDTO.boardId),
                memberId = extractDigits(commandBoardRecommentCreateByGifDTO.memberId),
                boardCommentId = extractDigits(commandBoardRecommentCreateByGifDTO.boardId),
            )

        val boardRecommentEntity = commandBoardRecommentRepository.save(boardRecomment)

        val commandFileDTO =
            boardRecommentEntity.boardRecommentId?.let {
                CommandFileDTO(
                    fileTargetType = FileTargetType.BOARD_RECOMMENT,
                    fileTargetId = formattedBoardRecommentId(it),
                    memberId = commandBoardRecommentCreateByGifDTO.memberId,
                )
            }

        commandFileDTO?.let { commandFileService.createSingleFile(file, commandFileDTO) }

        cacheManager.getCache("boardRecomments/${commandBoardRecommentCreateByGifDTO.boardCommentId}")?.clear()
        cacheManager.getCache("boardRecomments/${commandBoardRecommentCreateByGifDTO.memberId}")?.clear()
    }

    @Transactional
//    @CacheEvict(value = ["/boardComment/{boardComment.boardId}"], allEntries = true)
    override fun deleteBoardRecomment(commandBoardRecommentDeleteDTO: CommandBoardRecommentDeleteDTO) {
        val boardRecommentId = extractDigits(commandBoardRecommentDeleteDTO.boardRecommentId)
        val boardRecomment =
            commandBoardRecommentRepository
                .findById(
                    boardRecommentId,
                ).orElseThrow { CommonException(ErrorCode.NOT_FOUND_BOARD_RECOMMENT) }

        boardRecomment.apply {
            active = false
        }

        // 설명. 내용이 비어있을 때, 파일 지우러 감
        boardRecomment.boardRecommentContents?.let {
            val commandFileDTO =
                CommandFileDTO(
                    fileTargetType = FileTargetType.BOARD_COMMENT,
                    fileTargetId = formattedBoardRecommentId(boardRecommentId),
                    memberId = commandBoardRecommentDeleteDTO.memberId,
                )
            commandFileService.deleteFile(commandFileDTO)
        }

        cacheManager.getCache("boardRecomments/${commandBoardRecommentDeleteDTO.boardCommentId}")?.clear()
        cacheManager.getCache("boardRecomments/${commandBoardRecommentDeleteDTO.memberId}")?.clear()
    }

    fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()

    fun formattedBoardRecommentId(boardId: Long): String = "BOA-REC-${boardId.toString().padStart(8, '0') ?: "00000000"}"
}