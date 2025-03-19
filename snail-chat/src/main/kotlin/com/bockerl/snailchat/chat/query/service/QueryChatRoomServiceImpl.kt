package com.bockerl.snailchat.chat.query.service

import com.bockerl.snailchat.chat.query.dto.request.QueryGroupChatRoomRequestDto
import com.bockerl.snailchat.chat.query.dto.request.QueryPersonalChatRoomRequestDto
import com.bockerl.snailchat.chat.query.dto.response.QueryGroupChatRoomResponseDto
import com.bockerl.snailchat.chat.query.dto.response.QueryPersonalChatRoomResponseDto
import com.bockerl.snailchat.chat.query.repository.queryGroupChatRoom.QueryGroupChatRoomRepository
import com.bockerl.snailchat.chat.query.repository.queryPersonalChatRoom.QueryPersonalChatRoomRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class QueryChatRoomServiceImpl(
    private val queryPersonalChatRoomRepository: QueryPersonalChatRoomRepository,
    private val queryGroupChatRoomRepository: QueryGroupChatRoomRepository,
    private val queryChatMessageService: QueryChatMessageService,
) : QueryChatRoomService {
    override fun getPersonalChatRoomList(
        queryPersonalChatRoomRequestDto: QueryPersonalChatRoomRequestDto,
    ): List<QueryPersonalChatRoomResponseDto> {
        val personalChatRoomByMemberId =
            if (queryPersonalChatRoomRequestDto.lastId == null) {
                queryPersonalChatRoomRepository.findLatestPersonalChatRoomsByMemberId(
                    queryPersonalChatRoomRequestDto.memberId,
                    queryPersonalChatRoomRequestDto.pageSize,
                )
            } else {
                // 다음 페이지 메시지 조회 ( lastId != null )
                queryPersonalChatRoomRepository.findPreviousPersonalChatRoomsByMemberId(
                    queryPersonalChatRoomRequestDto.memberId,
                    ObjectId(queryPersonalChatRoomRequestDto.lastId),
                    queryPersonalChatRoomRequestDto.pageSize,
                )
            }

        // 최신 메시지 정보를 함께 가져와 DTO로 변환
        val queryPersonalChatRoomDTOList =
            personalChatRoomByMemberId.map { personalChatRoom ->
                val latestChatMessage = queryChatMessageService.getLatestChatMessageByChatRoomId(personalChatRoom.chatRoomId)

                QueryPersonalChatRoomResponseDto(
                    chatRoomId = personalChatRoom.chatRoomId.toString(),
                    chatRoomName =
                        personalChatRoom.chatRoomName
                            .filterKeys { it == queryPersonalChatRoomRequestDto.memberId } // 현재 memberId를 제외한 값
                            .values
                            .firstOrNull() ?: "알 수 없음",
                    chatRoomPhoto =
                        personalChatRoom.chatRoomPhoto
                            .filterKeys { it == queryPersonalChatRoomRequestDto.memberId } // 현재 memberId를 제외한 값
                            .values
                            .firstOrNull() ?: "사진 없음",
                    latestMessage = latestChatMessage?.message ?: "채팅 메시지가 없습니다.",
                    latestMessageTime = latestChatMessage?.createdAt ?: personalChatRoom.createdAt ?: Instant.now(),
//                    unreadCount =
//                        getUnreadMessageCount(
//                            personalChatRoom.chatRoomId,
//                            ObjectId(queryPersonalChatRoomRequestDto.memberId),
//                        ),
//                    isMuted = isChatRoomMuted(chatRoom.chatRoomId, ObjectId(queryPersonalChatRoomRequestDto.memberId)),
                )
            }

        return queryPersonalChatRoomDTOList
    }

    override fun getGroupChatRoomList(queryGroupChatRoomRequestDto: QueryGroupChatRoomRequestDto): List<QueryGroupChatRoomResponseDto> {
        val groupChatRoomByMemberId =
            if (queryGroupChatRoomRequestDto.lastId == null) {
                queryGroupChatRoomRepository.findLatestGroupChatRoomsByMemberId(
                    queryGroupChatRoomRequestDto.memberId,
                    queryGroupChatRoomRequestDto.pageSize,
                )
            } else {
                // 다음 페이지 메시지 조회 ( lastId != null )
                queryGroupChatRoomRepository.findPreviousGroupChatRoomsByMemberId(
                    queryGroupChatRoomRequestDto.memberId,
                    ObjectId(queryGroupChatRoomRequestDto.lastId),
                    queryGroupChatRoomRequestDto.pageSize,
                )
            }

        // 최신 메시지 정보를 함께 가져와 DTO로 변환
        val queryGrouplChatRoomDTOList =
            groupChatRoomByMemberId.map { groupChatRoom ->
                val latestChatMessage = queryChatMessageService.getLatestChatMessageByChatRoomId(groupChatRoom.chatRoomId)

                QueryGroupChatRoomResponseDto(
                    chatRoomId = groupChatRoom.chatRoomId.toString(),
                    chatRoomName = groupChatRoom.chatRoomName,
                    chatRoomPhoto = groupChatRoom.chatRoomPhoto,
                    latestMessage = latestChatMessage?.message ?: "채팅 메시지가 없습니다.",
                    latestMessageTime = latestChatMessage?.createdAt ?: groupChatRoom.createdAt ?: Instant.now(),
//                    unreadCount =
//                        getUnreadMessageCount(
//                            groupChatRoom.chatRoomId,
//                            ObjectId(queryGroupChatRoomRequestDto.memberId),
//                        ),
//                    isMuted = isChatRoomMuted(chatRoom.chatRoomId, ObjectId(queryGroupChatRoomRequestDto.memberId)),
                )
            }

        return queryGrouplChatRoomDTOList
    }
}