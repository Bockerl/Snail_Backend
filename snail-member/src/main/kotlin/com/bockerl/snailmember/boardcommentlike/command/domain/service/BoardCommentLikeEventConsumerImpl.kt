package com.bockerl.snailmember.boardcommentlike.command.domain.service

import com.bockerl.snailmember.boardcommentlike.command.application.service.BoardCommentLikeEventConsumer
import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.entity.BoardCommentLike
import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.enum.BoardCommentLikeActionType
import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.event.BoardCommentLikeEvent
import com.bockerl.snailmember.boardcommentlike.command.domain.repository.BoardCommentLikeRepository
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.mongodb.DuplicateKeyException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

@Service
class BoardCommentLikeEventConsumerImpl(
    private val boardCommentLikeRepository: BoardCommentLikeRepository,
//    @Value("\${spring.kafka.consumer.group-id}") private val groupId: String,
) : BoardCommentLikeEventConsumer {
    private val boardCommentLikeBuffer = mutableListOf<BoardCommentLike>()
    private val bufferSize = 100
    private val logger = KotlinLogging.logger {}

    @Transactional
    @KafkaListener(
        topics = ["board-like-events"],
        groupId = "snail-member",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun consume(
        @Payload event: BoardCommentLikeEvent,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        // 설명. 오프셋 커밋용
        acknowledgment: Acknowledgment,
    ) {
        logger.info { "received header: $partition" }
        // 설명. 멱등성 보장을 위한 try-catch문
        try {
            when (event.boardCommentLikeActionType) {
                BoardCommentLikeActionType.LIKE -> {
                    val like = BoardCommentLike(memberId = event.memberId, boardId = event.boardId, boardCommentId = event.boardCommentId)
                    boardCommentLikeBuffer.add(like)
                    if (boardCommentLikeBuffer.size >= bufferSize) {
                        boardCommentLikeRepository.saveAll(boardCommentLikeBuffer)
                        boardCommentLikeBuffer.clear()
                    }
                }

                BoardCommentLikeActionType.UNLIKE -> {
                    boardCommentLikeRepository.deleteByMemberIdAndBoardId(event.memberId, event.boardId)
                }
            }
            // 설명. 오프셋 수동 커밋
            acknowledgment.acknowledge()
        } catch (e: DuplicateKeyException) {
            logger.error(e) { "Duplicate key exception: $e.message" }
            throw CommonException(ErrorCode.DATA_INTEGRITY_VIOLATION)
        } catch (e: Exception) {
            // 설명. 재시도 로직 or DLQ(Dead Letter Queue) 추가 예정
            logger.error(e) { "예외 발생: ${e.message}" }
            throw e
        }
    }
}