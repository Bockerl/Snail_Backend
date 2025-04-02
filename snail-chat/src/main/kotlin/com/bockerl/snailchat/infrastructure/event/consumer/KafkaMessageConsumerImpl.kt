package com.bockerl.snailchat.infrastructure.event.consumer

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDTO
import com.bockerl.snailchat.chat.command.application.service.CommandChatMessageService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class KafkaMessageConsumerImpl(
    private val commandChatMessageService: CommandChatMessageService,
) : KafkaMessageConsumer {
    private val logger = KotlinLogging.logger { }

    @KafkaListener(
        topics = ["\${spring.kafka.topic.personal-chat}"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory",
    )
    override fun consumeChatMessage(chatMessageDTO: CommandChatMessageRequestDTO) {
        commandChatMessageService.sendToStomp(chatMessageDTO)
        logger.info { "Kafka에서 메시지 수신: $chatMessageDTO" }
    }
}