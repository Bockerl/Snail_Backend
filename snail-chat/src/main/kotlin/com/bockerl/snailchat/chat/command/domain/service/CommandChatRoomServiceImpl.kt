package com.bockerl.snailchat.chat.command.domain.service

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomCreateRequestDto
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
    override fun createChatRoom(commandChatRoomCreateRequestDto: CommandChatRoomCreateRequestDto) {
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
        if (commandChatRoomCreateRequestDto.chatRoomType == CommandChatRoomType.PERSONAL) {
            val chatRoom =
                ChatRoom(
                    chatRoomName = participants[0].memberNickname,
                    chatRoomType = commandChatRoomCreateRequestDto.chatRoomType,
                    creator = creator,
                    participants = participants,
                )

            commandChatRoomRepository.save(chatRoom)
        }
    }
}