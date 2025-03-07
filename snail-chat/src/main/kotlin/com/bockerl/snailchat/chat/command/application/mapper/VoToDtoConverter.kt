package com.bockerl.snailchat.chat.command.application.mapper

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDto
import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomRequestDto
import com.bockerl.snailchat.chat.command.domain.aggregate.vo.request.CommandChatRoomCreateRequestVo
import com.bockerl.snailchat.chat.command.domain.aggregate.vo.request.SendMessageRequestVo
import org.springframework.stereotype.Component

@Component
class VoToDtoConverter {
    // 메시지 전송 vo -> dto
    fun sendMessageRequestVoToDto(
        requestVo: SendMessageRequestVo,
        chatRoomId: String,
    ) = CommandChatMessageRequestDto(
        chatRoomId = chatRoomId, // DestinationVariable 받아온 chatRoomId 넣어줌
        sender = requestVo.sender,
        message = requestVo.message ?: throw RuntimeException("message가 존재하지 않습니다."),
        messageType = requestVo.messageType,
    )

    fun commandChatRoomCreateRequestVoTODto(requestVo: CommandChatRoomCreateRequestVo) =
        CommandChatRoomRequestDto(
            chatRoomName = requestVo.chatRoomName ?: throw RuntimeException("채팅방 이름을 다시 작성하세요."),
            chatRoomType = requestVo.chatRoomType,
        )
}