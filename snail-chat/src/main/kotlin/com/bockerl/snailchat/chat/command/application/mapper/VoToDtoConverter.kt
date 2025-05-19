package com.bockerl.snailchat.chat.command.application.mapper

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageKeyRequestDTO
import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDTO
import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomDeleteRequestDTO
import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomJoinRequestDTO
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
    ) = CommandChatMessageRequestDTO(
        chatRoomId = chatRoomId, // DestinationVariable 받아온 chatRoomId 넣어줌
        memberId = requestVo.memberId,
        memberNickname = requestVo.memberNickname,
        memberPhoto = requestVo.memberPhoto,
        message = requestVo.message,
        messageType = requestVo.messageType,
    )

    fun commandChatRoomDeleteRequestVoTODTO(
        requestVo: CommandChatRoomDeleteRequestVo,
        memberId: String,
        memberNickname: String,
        memberPhoto: String,
    ) = CommandChatRoomDeleteRequestDTO(
        chatRoomId = requestVo.chatRoomId,
        memberId = memberId,
        memberNickname = memberNickname,
        memberPhoto = memberPhoto,
    )

    fun commandChatRoomJoinRequestVoToDTO(
        requestVo: CommandChatRoomJoinRequestVo,
        memberId: String,
        memberNickname: String,
        memberPhoto: String,
    ) = CommandChatRoomJoinRequestDTO(
        chatRoomId = requestVo.chatRoomId,
        memberId = memberId,
        memberNickname = memberNickname,
        memberPhoto = memberPhoto,
    )

    fun sendMessageRequestVoAndKeyToDTO(
        requestVo: SendMessageRequestVo,
        chatRoomId: String,
        idempotencyKey: String,
    ) = CommandChatMessageKeyRequestDTO(
        chatRoomId = chatRoomId, // DestinationVariable 받아온 chatRoomId 넣어줌
        memberId = requestVo.memberId,
        memberNickname = requestVo.memberNickname,
        memberPhoto = requestVo.memberPhoto,
        chatRoomType = requestVo.chatRoomType,
        message = requestVo.message,
        messageType = requestVo.messageType,
        idempotencyKey = idempotencyKey,
    )
}