package com.bockerl.snailmember.infrastructure.event.handler

import com.bockerl.snailmember.boardcommentlike.command.application.dto.CommandBoardCommentLikeDTO
import com.bockerl.snailmember.boardcommentlike.command.application.service.CommandBoardCommentLikeService
import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.enum.BoardCommentLikeActionType
import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.event.BoardCommentLikeEvent
import com.bockerl.snailmember.boardlike.command.application.dto.CommandBoardLikeDTO
import com.bockerl.snailmember.boardlike.command.application.service.CommandBoardLikeService
import com.bockerl.snailmember.boardlike.command.domain.aggregate.enum.BoardLikeActionType
import com.bockerl.snailmember.boardlike.command.domain.aggregate.event.BoardLikeEvent
import com.bockerl.snailmember.common.BaseLikeEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class LikeEventHandler(
    private val commandBoardLikeService: CommandBoardLikeService,
    private val commandBoardCommentLikeService: CommandBoardCommentLikeService,
) {
    private val boardLikeBuffer = mutableListOf<CommandBoardLikeDTO>()
    private val boardCommentLikeBuffer = mutableListOf<CommandBoardCommentLikeDTO>()
    private val bufferSize = 1
    private val logger = KotlinLogging.logger {}

    @Transactional
    fun handle(event: BaseLikeEvent) {
        when (event) {
            is BoardLikeEvent -> {
                when (event.boardLikeActionType) {
                    BoardLikeActionType.LIKE -> {
                        // 설명. dto로 바꾸면 될듯?
                        val like = CommandBoardLikeDTO(memberId = event.memberId, boardId = event.boardId)
                        boardLikeBuffer.add(like)
                        if (boardLikeBuffer.size >= bufferSize) {
                            commandBoardLikeService.createBoardLikeEventList(boardLikeBuffer)
                            boardLikeBuffer.clear()
                        }
                    }

                    BoardLikeActionType.UNLIKE -> {
                        commandBoardLikeService.deleteBoardLikeEvent(
                            CommandBoardLikeDTO(
                                memberId = event.memberId,
                                boardId = event.boardId,
                            ),
                        )
                    }
                }
            }

            is BoardCommentLikeEvent -> {
                when (event.boardCommentLikeActionType) {
                    BoardCommentLikeActionType.LIKE -> {
                        val like =
                            CommandBoardCommentLikeDTO(
                                memberId = event.memberId,
                                boardId = event.boardId,
                                boardCommentId = event.boardCommentId,
                            )
                        boardCommentLikeBuffer.add(like)
                        if (boardCommentLikeBuffer.size >= bufferSize) {
                            commandBoardCommentLikeService.createBoardCommentLikeEventList(boardCommentLikeBuffer)
                            boardCommentLikeBuffer.clear()
                        }
                    }

                    BoardCommentLikeActionType.UNLIKE -> {
                        commandBoardCommentLikeService.deleteBoardCommentLikeEvent(
                            CommandBoardCommentLikeDTO(
                                memberId = event.memberId,
                                boardCommentId = event.boardCommentId,
                                boardId = event.boardId,
                            ),
                        )
                    }
                }
            }

            else -> {
                logger.warn { "Unknown event type received: ${event.javaClass}" }
            }
        }
    }

    private fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()
}