package com.bockerl.snailchat.infrastructure.event.consumer

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDTO
import com.bockerl.snailchat.chat.command.application.service.CommandChatMessageService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class KafkaMessageConsumerImpl(
    private val commandChatMessageService: CommandChatMessageService,
) : KafkaMessageConsumer {
    private val logger = KotlinLogging.logger { }

    @Transactional
    @KafkaListener(
        topics = ["\${spring.kafka.topic.personal-chat}"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun consumeChatMessage(
        chatMessageDTO: CommandChatMessageRequestDTO,
        acknowledgment: Acknowledgment,
    ) {
        try {
            commandChatMessageService.sendToStomp(chatMessageDTO)
            acknowledgment.acknowledge()
            logger.info { "Kafka 메시지 수신 성공 " }
        } catch (e: Exception) {
            logger.error(e) { "Kafka 메시지 처리 중 예외 발생" }
            throw e
        }
    }
}