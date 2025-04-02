package com.bockerl.snailchat.infrastructure.producer

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDTO

interface KafkaMessageProducer {
    fun sendMessageByKafka(
        topic: String,
        key: String,
        chatMessageDTO: CommandChatMessageRequestDTO,
    )
}