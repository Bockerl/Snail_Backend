package com.bockerl.snailmember.infrastructure.event.consumer

import com.bockerl.snailmember.boardlike.command.domain.aggregate.event.BoardLikeEvent
import com.bockerl.snailmember.boardlike.command.domain.repository.BoardLikeRepository
import com.bockerl.snailmember.infrastructure.event.handler.LikeEventHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

@Service
class LikeEventConsumerImpl(
    private val boardLikeRepository: BoardLikeRepository,
    private val likeEventHandler: LikeEventHandler,
//    @Value("\${spring.kafka.consumer.group-id}") private val groupId: String,
) : LikeEventConsumer {
    private val logger = KotlinLogging.logger {}

    @Transactional
    @KafkaListener(
        // 설명. 이 토픽에서 좋아요 이벤트는 다 받게할 것
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
            likeEventHandler.handle(event)
            // 설명. 오프셋 수동 커밋
            acknowledgment.acknowledge()
        } catch (e: Exception) {
            // 설명. 재시도 로직 or DLQ(Dead Letter Queue) 추가 예정
            logger.error(e) { "예외 발생: ${e.message}" }
            throw e
        }
    }
}