package com.bockerl.snailmember.boardlike.command.domain.service

import com.bockerl.snailmember.boardlike.command.application.service.BoardLikeEventConsumer
import com.bockerl.snailmember.boardlike.command.domain.aggregate.entity.BoardLike
import com.bockerl.snailmember.boardlike.command.domain.aggregate.enum.ActionType
import com.bockerl.snailmember.boardlike.command.domain.aggregate.event.BoardLikeEvent
import com.bockerl.snailmember.boardlike.command.domain.repository.BoardLikeRepository
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.mongodb.DuplicateKeyException
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class BoardLikeEventConsumerImpl(
    private val boardLikeRepository: BoardLikeRepository,
) : BoardLikeEventConsumer {
    private val boardLikeBuffer = mutableListOf<BoardLike>()
    private val bufferSize = 100

    @KafkaListener(topics = ["board-like-events"], groupId = "board-like-group")
    fun consume(event: BoardLikeEvent) {
        // 설명. 멱등성 보장을 위한 try-catch문
        try {
            when (event.actionType) {
                ActionType.LIKE -> {
                    val like = BoardLike(memberId = event.memberId, boardId = event.boardId)
                    boardLikeBuffer.add(like)
                    if (boardLikeBuffer.size >= bufferSize) {
                        boardLikeRepository.saveAll(boardLikeBuffer)
                        boardLikeBuffer.clear()
                    }
                }

                ActionType.UNLIKE -> {
                    boardLikeRepository.deleteByMemberIdAndBoardId(event.memberId, event.boardId)
                }
            }
        } catch (e: DuplicateKeyException) {
            throw CommonException(ErrorCode.DATA_INTEGRITY_VIOLATION)
        }
    }
}