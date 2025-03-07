package com.bockerl.snailchat.chat.command.domain.service

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomCreateRequestDto
import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomDeleteRequestDto
import com.bockerl.snailchat.chat.command.application.service.CommandChatMessageService
import com.bockerl.snailchat.chat.command.application.service.CommandChatRoomService
import com.bockerl.snailchat.chat.command.domain.aggregate.entity.ChatRoom
import com.bockerl.snailchat.chat.command.domain.aggregate.entity.MemberInfo
import com.bockerl.snailchat.chat.command.domain.aggregate.enums.CommandChatRoomType
import com.bockerl.snailchat.chat.command.domain.repository.CommandChatRoomRepository
import com.bockerl.snailchat.common.exception.CommonException
import com.bockerl.snailchat.common.exception.ErrorCode
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class CommandChatRoomServiceImpl(
    private val commandChatRoomRepository: CommandChatRoomRepository,
    private val commanChatMessageService: CommandChatMessageService,
    private val simpleMessagingTemplate: SimpMessagingTemplate,
//    private val memberFeignClient: MemberFeignClient,
) : CommandChatRoomService {
    override fun createChatRoom(commandChatRoomCreateRequestDto: CommandChatRoomCreateRequestDto) {
        // FeignClient 적용 전 임시데이터
        if (commandChatRoomCreateRequestDto.chatRoomType == CommandChatRoomType.PERSONAL) {
            val participants =
                listOf(
                    MemberInfo(
                        memberId = "member-0001",
                        memberNickname = "Alice",
                        memberPhoto = "Alice.jpg",
                    ),
                    MemberInfo(
                        memberId = "member-0002",
                        memberNickname = "John",
                        memberPhoto = "John.jpg",
                    ),
                )

            // 개인 채팅방일 경우
            val chatRoom =
                ChatRoom(
                    chatRoomName = participants[1].memberNickname,
                    chatRoomType = commandChatRoomCreateRequestDto.chatRoomType,
                    chatRoomStatus = true,
                    participants = participants,
                )

            commandChatRoomRepository.save(chatRoom)
        }
    }

    override fun deleteChatRoom(commandChatRoomDeleteRequestDto: CommandChatRoomDeleteRequestDto) {
        val chatRoom =
            commandChatRoomRepository
                .findById(commandChatRoomDeleteRequestDto.chatRoomId)
                .orElseThrow { (CommonException(ErrorCode.NOT_FOUND_CHAT_ROOM)) }

        // 참가자 리스트에서 나가는 사용자 제거
        val updatedParticipants =
            chatRoom.participants.filter { it.memberId != commandChatRoomDeleteRequestDto.memberId }

        // 참가자 리스트 존재하지 않으면, 채팅방 상태 변경 후 메소드 종료
        if (updatedParticipants.isEmpty()) {
            val updatedChatRoom = chatRoom.copy(participants = updatedParticipants, chatRoomStatus = false)
            commandChatRoomRepository.save(updatedChatRoom)
            return
        }

        // 참가자가 남아있는 경우만 퇴장 채팅 저장
        commanChatMessageService.saveLeaveMessage(
            commandChatRoomDeleteRequestDto.chatRoomId,
            commandChatRoomDeleteRequestDto.memberId,
            commandChatRoomDeleteRequestDto.memberNickname,
        )

        val updatedChatRoom = chatRoom.copy(participants = updatedParticipants)
        commandChatRoomRepository.save(updatedChatRoom)

        // 채팅방 참여자들에게 수정된 채팅방 정보(채팅방 참여자 수등) 제공
        simpleMessagingTemplate.convertAndSend(
            "topic/chatRoom/$commandChatRoomDeleteRequestDto.chatRoomId",
            updatedChatRoom,
        )
    }
}