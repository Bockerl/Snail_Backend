package com.bockerl.snailchat.chat.command.application.mapper

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDto
import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomCreateRequestDto
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
        memberId = requestVo.memberId,
        memberNickname = requestVo.memberNickname,
        memberPhoto = requestVo.memberPhoto,
        message = requestVo.message,
        messageType = requestVo.messageType,
    )

    fun commandChatRoomCreateRequestVoTODto(
        requestVo: CommandChatRoomCreateRequestVo,
        memberId: String,
        memberNickname: String,
        memberPhoto: String,
    ) = CommandChatRoomCreateRequestDto(
        chatRoomName = requestVo.chatRoomName,
        chatRoomType = requestVo.chatRoomType,
        memberId = memberId,
        memberNickname = memberNickname,
        memberPhoto = memberPhoto,
    )
}