package com.bockerl.snailchat.chat.command.domain.service

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomCreateRequestDto
import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomDeleteRequestDto
import com.bockerl.snailchat.chat.command.application.service.CommandChatMessageService
import com.bockerl.snailchat.chat.command.application.service.CommandChatRoomService
import com.bockerl.snailchat.chat.command.domain.aggregate.entity.GroupChatRoom
import com.bockerl.snailchat.chat.command.domain.aggregate.entity.PersonalChatRoom
import com.bockerl.snailchat.chat.command.domain.aggregate.enums.ChatRoomType
import com.bockerl.snailchat.chat.command.domain.aggregate.vo.MemberInfo
import com.bockerl.snailchat.chat.command.domain.repository.CommandGroupChatRoomRepository
import com.bockerl.snailchat.chat.command.domain.repository.CommandPersonalChatRoomRepository
import com.bockerl.snailchat.common.exception.CommonException
import com.bockerl.snailchat.common.exception.ErrorCode
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class CommandChatRoomServiceImpl(
    private val commandPersonalChatRoomRepository: CommandPersonalChatRoomRepository,
    private val commandGroupChatRoomRepository: CommandGroupChatRoomRepository,
    private val commanChatMessageService: CommandChatMessageService,
    private val simpleMessagingTemplate: SimpMessagingTemplate,
//    private val memberFeignClient: MemberFeignClient,
) : CommandChatRoomService {
    override fun createPersonalChatRoom(commandChatRoomCreateRequestDto: CommandChatRoomCreateRequestDto) {
        // FeignClient 적용 전 임시데이터
        val participants =
            listOf(
                MemberInfo(
                    memberId = commandChatRoomCreateRequestDto.memberId,
                    memberNickname = commandChatRoomCreateRequestDto.memberNickname,
                    memberPhoto = commandChatRoomCreateRequestDto.memberPhoto,
                ),
                // 향후 FeignClient로 DM을 받는 사람의 정보를 받아와야함
                MemberInfo(
                    memberId = "member-0002",
                    memberNickname = "John",
                    memberPhoto = "John.jpg",
                ),
            )

        val personalChatRoom =
            PersonalChatRoom(
                // Map 형태로 선언해서 조회하는 사용자에 따라서 채팅방 이름 달라지도록 선언
                chatRoomName =
                    mapOf(
                        participants[0].memberId to participants[1].memberNickname,
                        participants[1].memberId to participants[0].memberNickname,
                    ),
                chatRoomType = ChatRoomType.PERSONAL,
                chatRoomStatus = true,
                participants = participants,
            )

        commandPersonalChatRoomRepository.save(personalChatRoom)
    }

    override fun createGroupChatRoom(commandChatRoomCreateRequestDto: CommandChatRoomCreateRequestDto) {
        // FeignClient 적용 전 임시데이터
        val meetingName = "Gangnam Climbing"
        val meetingCategory = "Climbing"

        val participants =
            listOf(
                MemberInfo(
                    memberId = commandChatRoomCreateRequestDto.memberId,
                    memberNickname = commandChatRoomCreateRequestDto.memberNickname,
                    memberPhoto = commandChatRoomCreateRequestDto.memberPhoto,
                ),
            )

        val groupChatRoom =
            GroupChatRoom(
                chatRoomName = meetingName,
                chatRoomType = ChatRoomType.GROUP,
                chatRoomCategory = meetingCategory,
                chatRoomStatus = true,
                participants = participants,
                participantsNum = 1,
            )

        commandGroupChatRoomRepository.save(groupChatRoom)
    }

    override fun deletePersonalChatRoom(commandChatRoomDeleteRequestDto: CommandChatRoomDeleteRequestDto) {
        val chatRoom =
            commandPersonalChatRoomRepository
                .findById(commandChatRoomDeleteRequestDto.chatRoomId)
                .orElseThrow { (CommonException(ErrorCode.NOT_FOUND_CHAT_ROOM)) }

        // 참가자 리스트에서 나가는 사용자 제거
        val updatedParticipants =
            chatRoom.participants.filter { it.memberId != commandChatRoomDeleteRequestDto.memberId }

        // 참가자 리스트 존재하지 않으면, 채팅방 상태 변경 후 메소드 종료
        if (updatedParticipants.isEmpty()) {
            val updatedChatRoom = chatRoom.copy(participants = updatedParticipants, chatRoomStatus = false)
            commandPersonalChatRoomRepository.save(updatedChatRoom)
            return
        }

        // 참가자가 남아있는 경우만 퇴장 채팅 저장
        commanChatMessageService.saveLeaveMessage(
            commandChatRoomDeleteRequestDto.chatRoomId,
            commandChatRoomDeleteRequestDto.memberId,
            commandChatRoomDeleteRequestDto.memberNickname,
            commandChatRoomDeleteRequestDto.memberPhoto,
        )

        val updatedChatRoom = chatRoom.copy(participants = updatedParticipants)
        commandPersonalChatRoomRepository.save(updatedChatRoom)

        // 채팅방 참여자들에게 수정된 채팅방 정보(채팅방 참여자 수등) 제공
        simpleMessagingTemplate.convertAndSend(
            "topic/chatRoom/$commandChatRoomDeleteRequestDto.chatRoomId",
            updatedChatRoom,
        )
    }

    override fun deleteGroupChatRoom(commandChatRoomDeleteRequestDto: CommandChatRoomDeleteRequestDto) {
        val chatRoom =
            commandGroupChatRoomRepository
                .findById(commandChatRoomDeleteRequestDto.chatRoomId)
                .orElseThrow { (CommonException(ErrorCode.NOT_FOUND_CHAT_ROOM)) }

        // 참가자 리스트에서 나가는 사용자 제거
        val updatedParticipants =
            chatRoom.participants.filter { it.memberId != commandChatRoomDeleteRequestDto.memberId }

        // 참가자 리스트 존재하지 않으면, 채팅방 상태 변경 후 메소드 종료
        if (updatedParticipants.isEmpty()) {
            val updatedChatRoom = chatRoom.copy(participants = updatedParticipants, chatRoomStatus = false)
            commandGroupChatRoomRepository.save(updatedChatRoom)
            return
        }

        // 참가자가 남아있는 경우만 퇴장 채팅 저장
        commanChatMessageService.saveLeaveMessage(
            commandChatRoomDeleteRequestDto.chatRoomId,
            commandChatRoomDeleteRequestDto.memberId,
            commandChatRoomDeleteRequestDto.memberNickname,
            commandChatRoomDeleteRequestDto.memberPhoto,
        )

        val updatedChatRoom = chatRoom.copy(participants = updatedParticipants)
        commandGroupChatRoomRepository.save(updatedChatRoom)

        // 채팅방 참여자들에게 수정된 채팅방 정보(채팅방 참여자 수등) 제공
        simpleMessagingTemplate.convertAndSend(
            "topic/chatRoom/$commandChatRoomDeleteRequestDto.chatRoomId",
            updatedChatRoom,
        )
    }
}