package com.bockerl.snailchat.infrastructure.producer

import com.bockerl.snailchat.chat.command.application.dto.ChatMessageDTO

interface KafkaMessageProducer {
    fun sendMessageByKafka(
        topic: String,
        key: String,
        chatMessageDTO: ChatMessageDTO,
    )
}