package com.bockerl.snailchat.infrastructure.event.consumer

import com.bockerl.snailchat.chat.command.application.dto.ChatMessageDTO

interface KafkaMessageConsumer {
    fun consumeChatMessage(chatMessageDTO: ChatMessageDTO)
}