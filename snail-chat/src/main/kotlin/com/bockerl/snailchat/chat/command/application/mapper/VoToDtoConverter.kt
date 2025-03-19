package com.bockerl.snailchat.chat.command.application.mapper

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDto
import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomDeleteRequestDto
import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomJoinRequestDto
import com.bockerl.snailchat.chat.command.domain.aggregate.vo.request.CommandChatRoomDeleteRequestVo
import com.bockerl.snailchat.chat.command.domain.aggregate.vo.request.CommandChatRoomJoinRequestVo
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

    fun commandChatRoomDeleteRequestVoTODto(
        requestVo: CommandChatRoomDeleteRequestVo,
        memberId: String,
        memberNickname: String,
        memberPhoto: String,
    ) = CommandChatRoomDeleteRequestDto(
        chatRoomId = requestVo.chatRoomId,
        memberId = memberId,
        memberNickname = memberNickname,
        memberPhoto = memberPhoto,
    )

    fun commandChatRoomJoinRequestVoToDto(
        requestVo: CommandChatRoomJoinRequestVo,
        memberId: String,
        memberNickname: String,
        memberPhoto: String,
    ) = CommandChatRoomJoinRequestDto(
        chatRoomId = requestVo.chatRoomId,
        memberId = memberId,
        memberNickname = memberNickname,
        memberPhoto = memberPhoto,
    )
}