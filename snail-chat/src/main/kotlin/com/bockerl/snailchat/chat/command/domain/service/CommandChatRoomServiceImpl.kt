package com.bockerl.snailchat.chat.command.domain.service

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomRequestDto
import com.bockerl.snailchat.chat.command.application.service.CommandChatRoomService
import com.bockerl.snailchat.chat.command.domain.aggregate.entity.ChatRoom
import com.bockerl.snailchat.chat.command.domain.aggregate.entity.MemberInfo
import com.bockerl.snailchat.chat.command.domain.aggregate.enums.CommandChatRoomType
import com.bockerl.snailchat.chat.command.domain.repository.CommandChatRoomRepository
import org.springframework.stereotype.Service

@Service
class CommandChatRoomServiceImpl(
    private val commandChatRoomRepository: CommandChatRoomRepository,
//    private val memberFeignClient: MemberFeignClient,
) : CommandChatRoomService {
    override fun createChatRoom(commandChatRoomRequestDto: CommandChatRoomRequestDto) {
        // FeignClient 적용 전 임시데이터
        val creator =
            MemberInfo(
                memberId = "member-0001",
                memberNickname = "Alice",
                memberPhoto = "Alice.jpg",
            )

        val participants =
            listOf(
                MemberInfo(
                    memberId = "member-0002",
                    memberNickname = "John",
                    memberPhoto = "John.jpg",
                ),
            )

        // 개인 채팅방일 경우
        if (commandChatRoomRequestDto.chatRoomType == CommandChatRoomType.PERSONAL) {
            val chatRoom =
                ChatRoom(
                    chatRoomName = commandChatRoomRequestDto.chatRoomName,
                    chatRoomType = commandChatRoomRequestDto.chatRoomType,
                    creator = creator,
                    participants = participants,
                )

            commandChatRoomRepository.save(chatRoom)
        }
    }
}