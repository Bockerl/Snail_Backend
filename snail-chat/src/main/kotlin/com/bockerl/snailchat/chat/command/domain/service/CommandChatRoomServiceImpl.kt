package com.bockerl.snailchat.chat.command.domain.service

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomCreateRequestDto
import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomDeleteRequestDto
import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomJoinRequestDto
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
import org.bson.types.ObjectId
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class CommandChatRoomServiceImpl(
    private val commandPersonalChatRoomRepository: CommandPersonalChatRoomRepository,
    private val commandGroupChatRoomRepository: CommandGroupChatRoomRepository,
    private val commandChatMessageService: CommandChatMessageService,
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
                chatRoomPhoto =
                    mapOf(
                        participants[0].memberId to participants[1].memberPhoto,
                        participants[1].memberId to participants[0].memberPhoto,
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
        val meetingPhoto = "Climbing.jpg"
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
                chatRoomPhoto = meetingPhoto,
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
                .findById(ObjectId(commandChatRoomDeleteRequestDto.chatRoomId))
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
        commandChatMessageService.saveLeaveMessage(
            chatRoom.chatRoomId,
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
                .findById(ObjectId(commandChatRoomDeleteRequestDto.chatRoomId))
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
        commandChatMessageService.saveLeaveMessage(
            chatRoom.chatRoomId,
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

    override fun joinGroupChatRoom(commandChatRoomJoinRequestDto: CommandChatRoomJoinRequestDto) {
        val chatRoom =
            commandGroupChatRoomRepository
                .findById(ObjectId(commandChatRoomJoinRequestDto.chatRoomId))
                .orElseThrow { (CommonException(ErrorCode.NOT_FOUND_CHAT_ROOM)) }

        if (chatRoom.participants.any { it.memberId == commandChatRoomJoinRequestDto.memberId }) {
            return
        }

        val newMemberinfo =
            MemberInfo(
                commandChatRoomJoinRequestDto.memberId,
                commandChatRoomJoinRequestDto.memberNickname,
                commandChatRoomJoinRequestDto.memberPhoto,
            )

        val updatedChatRoom =
            chatRoom.copy(
                participants = chatRoom.participants + newMemberinfo,
                participantsNum = chatRoom.participantsNum + 1,
            )

        // 추가된 참가자와 참가자 수를 반영하여 update
        commandGroupChatRoomRepository.save(updatedChatRoom)

        // 새 사용자 입장 메시지
        val enterMessage =
            commandChatMessageService.saveEnterMessage(
                chatRoom.chatRoomId,
                commandChatRoomJoinRequestDto.memberId,
                commandChatRoomJoinRequestDto.memberNickname,
                commandChatRoomJoinRequestDto.memberPhoto,
            )

        // 채팅방 구독자들에게 입장 메시지 전송
        simpleMessagingTemplate.convertAndSend("/topic/message/${chatRoom.chatRoomId}", enterMessage)

        // 수정된 채팅방 정보 업데이트를 채팅방 내 모든 사용자들에게 전달
        simpleMessagingTemplate.convertAndSend("/topic/room/${chatRoom.chatRoomId}/update", updatedChatRoom)

        // 새로운 참여자들도 채팅방 정보 업데이트 - 확인후 구독
        simpleMessagingTemplate.convertAndSend("/topic/member/${commandChatRoomJoinRequestDto.memberId}/update", updatedChatRoom.chatRoomId)
    }
}