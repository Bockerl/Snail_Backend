package com.bockerl.snailchat.infrastructure.event.consumer

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDTO

interface KafkaMessageConsumer {
    fun consumeChatMessage(chatMessageDTO: CommandChatMessageRequestDTO)
}