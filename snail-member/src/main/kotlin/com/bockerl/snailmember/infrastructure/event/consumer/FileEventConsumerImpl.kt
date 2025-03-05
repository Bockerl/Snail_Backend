package com.bockerl.snailmember.infrastructure.event.consumer

import com.bockerl.snailmember.common.event.BaseFileCreatedEvent
import com.bockerl.snailmember.file.command.domain.aggregate.event.FileDeletedEvent
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
        topics = ["file-events"],
        groupId = "snail-member",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun consumeCreate(
        @Payload event: BaseFileCreatedEvent,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        acknowledgment: Acknowledgment,
    ) {
        logger.info { "received header: $partition" }
        try {
            fileEventProcessor.processCreate(event)
            // 설명. 오프셋 수동 커밋
            acknowledgment.acknowledge()
        } catch (e: Exception) {
            // 설명. 재시도 로직 or DLQ(Dead Letter Queue) 추가 예정
            logger.error(e) { "예외 발생: ${e.message}" }
            throw e
        }
    }

    @Transactional
    @KafkaListener(
        topics = ["file-events"],
        groupId = "snail-member",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun consumeDelete(
        @Payload event: FileDeletedEvent,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        acknowledgment: Acknowledgment,
    ) {
        logger.info { "received header: $partition" }
        // 설명. 멱등성 보장을 위한 try-catch문
        try {
            fileEventProcessor.processDelete(event)
            // 설명. 오프셋 수동 커밋
            acknowledgment.acknowledge()
        } catch (e: Exception) {
            logger.error(e) { "예외 발생: ${e.message}" }
            throw e
        }
    }
}