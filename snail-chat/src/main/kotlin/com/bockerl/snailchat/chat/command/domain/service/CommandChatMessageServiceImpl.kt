package com.bockerl.snailchat.chat.command.domain.service

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDto
import com.bockerl.snailchat.chat.command.application.service.CommandChatMessageService
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class CommandChatMessageServiceImpl(
    private val simpleMessagingTemplate: SimpMessagingTemplate,
) : CommandChatMessageService {
    // STOMP를 통한 메시지 전송
    override fun sendMessage(
        roomId: String,
        updateMessageDto: CommandChatMessageRequestDto,
    ) {
        // 해당 경로를 구독하고 있는 Client들에게 Message 전송
        simpleMessagingTemplate.convertAndSend("/topic/message/$roomId", updateMessageDto)
    }
}