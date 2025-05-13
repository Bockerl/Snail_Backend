package com.bockerl.snailmember.infrastructure.event.consumer

import com.bockerl.snailmember.common.event.BaseFileCreatedEvent
import com.bockerl.snailmember.file.command.domain.aggregate.event.FileCreatedEvent
import com.bockerl.snailmember.file.command.domain.aggregate.event.FileDeletedEvent
import com.bockerl.snailmember.file.command.domain.aggregate.event.GatheringFileCreatedEvent
import com.bockerl.snailmember.infrastructure.event.processor.FileEventProcessor
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

@Service
class FileEventConsumerImpl(
    private val fileEventProcessor: FileEventProcessor,
) : FileEventConsumer {
    private val logger = KotlinLogging.logger {}

    @Transactional
    @KafkaListener(
//        topics = ["file-created-events"],
        topics = ["file-events"],
        groupId = "snail-member",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun consumeCreate(
        @Payload event: BaseFileCreatedEvent,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header("eventId") eventId: String,
        @Header("idempotencyKey") idempotencyKey: String?,
        acknowledgment: Acknowledgment,
    ) {
        logger.info { "Received event: $event, eventId: $eventId" }
        try {
            when (event) {
                is FileCreatedEvent -> fileEventProcessor.processCreate(event, eventId, idempotencyKey)
                is GatheringFileCreatedEvent -> fileEventProcessor.processCreate(event, eventId, idempotencyKey)
                is FileDeletedEvent -> fileEventProcessor.processDelete(event, eventId, idempotencyKey)
                else -> logger.warn { "알 수 없는 이벤트 타입: $event" }
            }
            // 설명. 오프셋 수동 커밋
            acknowledgment.acknowledge()
        } catch (e: Exception) {
            // 설명. 재시도 로직 or DLQ(Dead Letter Queue) 추가 예정
            logger.error(e) { "예외 발생: ${e.message}" }
            throw e
        }
    }

//    @Transactional
//    @KafkaListener(
// //        topics = ["file-deleted-events"],
//        topics = ["file-events"],
//        // groupId를 바꿀 가능성이 많다..
//        groupId = "snail-member",
//        containerFactory = "kafkaListenerContainerFactory",
//    )
//    fun consumeDelete(
//        @Payload event: FileDeletedEvent,
//        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
//        @Header("eventId") eventId: String,
//        @Header("idempotencyKey") idempotencyKey: String?,
//        acknowledgment: Acknowledgment,
//    ) {
//        logger.info { "received header: $partition" }
//        // 설명. 멱등성 보장을 위한 try-catch문
//        try {
//            fileEventProcessor.processDelete(event, eventId, idempotencyKey)
//            // 설명. 오프셋 수동 커밋
//            acknowledgment.acknowledge()
//        } catch (e: Exception) {
//            logger.error(e) { "예외 발생: ${e.message}" }
//            throw e
//        }
//    }
}