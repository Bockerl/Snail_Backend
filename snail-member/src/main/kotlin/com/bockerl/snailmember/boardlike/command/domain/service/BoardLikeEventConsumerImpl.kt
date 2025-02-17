package com.bockerl.snailmember.boardlike.command.domain.service

import com.bockerl.snailmember.boardlike.command.application.service.BoardLikeEventConsumer
import com.bockerl.snailmember.boardlike.command.domain.aggregate.entity.BoardLike
import com.bockerl.snailmember.boardlike.command.domain.aggregate.enum.ActionType
import com.bockerl.snailmember.boardlike.command.domain.aggregate.event.BoardLikeEvent
import com.bockerl.snailmember.boardlike.command.domain.repository.BoardLikeRepository
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
class BoardLikeEventConsumerImpl(
    private val boardLikeRepository: BoardLikeRepository,
//    @Value("\${spring.kafka.consumer.group-id}") private val groupId: String,
) : BoardLikeEventConsumer {
    private val boardLikeBuffer = mutableListOf<BoardLike>()
    private val bufferSize = 100
    private val logger = KotlinLogging.logger {}

    @Transactional
    @KafkaListener(
        topics = ["board-like-events"],
        groupId = "snail-member",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun consume(
        @Payload event: BoardLikeEvent,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        // 설명. 오프셋 커밋용
        acknowledgment: Acknowledgment,
    ) {
        logger.info { "received header: $partition" }
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