package com.bockerl.snailmember.boardcomment.command.domain.service

import com.bockerl.snailmember.boardcomment.command.application.service.CommandBoardCommentService
import com.bockerl.snailmember.boardcomment.command.domain.aggregate.entity.BoardComment
import com.bockerl.snailmember.boardcomment.command.domain.aggregate.vo.request.CommandBoardCommentCreateByGifRequestVO
import com.bockerl.snailmember.boardcomment.command.domain.aggregate.vo.request.CommandBoardCommentCreateRequestVO
import com.bockerl.snailmember.boardcomment.command.domain.aggregate.vo.request.CommandBoardCommentDeleteRequestVO
import com.bockerl.snailmember.boardcomment.command.domain.repository.CommandBoardCommentRepository
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.file.command.application.service.CommandFileService
import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType
import com.bockerl.snailmember.file.command.domain.aggregate.vo.CommandFileRequestVO
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
    override fun createBoardComment(commandBoardCommentCreateRequestVO: CommandBoardCommentCreateRequestVO) {
        val boardComment =
            BoardComment(
                boardCommentContents = commandBoardCommentCreateRequestVO.boardCommentContents,
                boardId = extractDigits(commandBoardCommentCreateRequestVO.boardId),
                memberId = extractDigits(commandBoardCommentCreateRequestVO.memberId),
            )

        commandBoardCommentRepository.save(boardComment)
        // 설명. 데이터 변경시 해당하는 해당 게시글 댓글들 캐시 초기화
        cacheManager.getCache("boardComments/${commandBoardCommentCreateRequestVO.boardId}")?.clear()
    }

    @Transactional
    override fun createBoardCommentByGif(
        commandBoardCommentCreateByGifRequestVO: CommandBoardCommentCreateByGifRequestVO,
        file: MultipartFile,
    ) {
        val boardComment =
            BoardComment(
                boardId = extractDigits(commandBoardCommentCreateByGifRequestVO.boardId),
                memberId = extractDigits(commandBoardCommentCreateByGifRequestVO.memberId),
            )

        val boardCommentEntity = commandBoardCommentRepository.save(boardComment)

        val commandFileRequestVO =
            boardCommentEntity.boardCommentId?.let {
                CommandFileRequestVO(
                    fileTargetType = FileTargetType.BOARD_COMMENT,
                    fileTargetId = it,
                    memberId = extractDigits(commandBoardCommentCreateByGifRequestVO.memberId),
                )
            }

        commandFileRequestVO?.let { commandFileService.uploadSingleFile(file, commandFileRequestVO) }

        cacheManager.getCache("boardComments/${commandBoardCommentCreateByGifRequestVO.boardId}")?.clear()
    }

    @Transactional
//    @CacheEvict(value = ["/boardComment/{boardComment.boardId}"], allEntries = true)
    override fun deleteBoardComment(commandBoardCommentDeleteRequestVO: CommandBoardCommentDeleteRequestVO) {
        val boardCommentId = extractDigits(commandBoardCommentDeleteRequestVO.boardCommentId)
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
            val commandFileRequestVO =
                CommandFileRequestVO(
                    fileTargetType = FileTargetType.BOARD_COMMENT,
                    fileTargetId = boardCommentId,
                    memberId = extractDigits(commandBoardCommentDeleteRequestVO.memberId),
                )
            commandFileService.deleteFile(commandFileRequestVO)
        }

        cacheManager.getCache("boardComments/${commandBoardCommentDeleteRequestVO.boardId}")?.clear()
    }

    fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()
}