package com.bockerl.snailchat.chat.command.application.mapper

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDto
import com.bockerl.snailchat.chat.command.domain.aggregate.vo.request.SendMessageRequestVo
import org.springframework.stereotype.Component

@Component
class VoToDtoConverter {
    // 메시지 전송 vo -> dto
    fun sendMessageRequestVoToDto(
        requestVo: SendMessageRequestVo,
        chatRoomId: String,
    ) = CommandChatMessageRequestDto(
        chatRoomId = chatRoomId, // DestinationVariable 받아온 roomId 넣어줌
        sender = requestVo.sender ?: throw RuntimeException("존재하지 않는 sender"),
        message = requestVo.message ?: throw RuntimeException("message가 존재하지 않습니다."),
        messageType = requestVo.messageType ?: throw RuntimeException("존재하지 않는 메시지 타입"),
    )
}