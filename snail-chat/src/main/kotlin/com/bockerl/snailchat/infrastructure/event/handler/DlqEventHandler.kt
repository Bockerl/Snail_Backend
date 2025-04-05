package com.bockerl.snailchat.infrastructure.event.handler

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDTO
import com.bockerl.snailchat.chat.command.application.service.CommandChatMessageService
import com.bockerl.snailchat.infrastructure.event.publisher.OutboxPublisher
import com.bockerl.snailchat.infrastructure.outbox.entity.Outbox
import com.bockerl.snailchat.infrastructure.outbox.service.OutboxService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class DlqEventHandler(
    private val outboxPublisher: OutboxPublisher,
    private val outboxService: OutboxService,
    private val commandChatMessageService: CommandChatMessageService,
) {
    private val logger = KotlinLogging.logger {}

    // 추가적으로 dlqRetryCount를 통해 3번 dlq 반복하면 DeadMessage(DB)로 옮기는 로직으로 변경 에정
    @KafkaListener(
        topics = ["chat.outbox.producer.dlq"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun handleProducerDlqMessage(event: Outbox) {
        logger.warn { "DLQ에서 Producer Outbox 복구 시작: ${event.outboxId}" }

        try {
            // 상태값과 retryCount를 수정해서 outbox 수정
            outboxService.changeStatusAndRetryCount(event)

            // 재처리를 위해 OutboxPublisher가 다시 주기적으로 읽어들이도록 저장만 함
            outboxPublisher.publishOutbox()
        } catch (e: Exception) {
            logger.error(e) { "DLQ 재처리 실패: ${event.outboxId}" }
        }
    }

    @KafkaListener(
        topics = ["chat.outbox.consumer.dlq"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun handleConsumerDlqMessage(
        chatMessageDTO: CommandChatMessageRequestDTO,
        acknowledgment: Acknowledgment,
    ) {
        logger.warn { "DLQ 재처리 시작: ${chatMessageDTO.message}" }

        try {
            commandChatMessageService.sendToStomp(chatMessageDTO)
            acknowledgment.acknowledge()
            logger.info { "DLQ 메시지 재처리 성공" }
        } catch (e: Exception) {
            logger.error(e) { "DLQ 메시지 재처리 실패" }
            throw e
        }
    }
}