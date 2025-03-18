package com.bockerl.snailchat.chat.query.service

import com.bockerl.snailchat.chat.query.dto.request.QueryPersonalChatRoomRequestDto
import com.bockerl.snailchat.chat.query.dto.response.QueryPersonalChatRoomResponseDto
import com.bockerl.snailchat.chat.query.repository.queryPersonalChatRoom.QueryPersonalChatRoomRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class QueryChatRoomServiceImpl(
    private val queryPersonalChatRoomRepository: QueryPersonalChatRoomRepository,
    private val queryChatMessageService: QueryChatMessageService,
) : QueryChatRoomService {
    override fun getPersonalChatRoomList(
        queryPersonalChatRoomRequestDto: QueryPersonalChatRoomRequestDto,
    ): List<QueryPersonalChatRoomResponseDto> {
        val personalChatRoomByMemberId =
            if (queryPersonalChatRoomRequestDto.lastId == null) {
                queryPersonalChatRoomRepository.findLatestPersonalChatRoomsByMemberId(
                    ObjectId(queryPersonalChatRoomRequestDto.memberId),
                    queryPersonalChatRoomRequestDto.pageSize,
                )
            } else {
                // 다음 페이지 메시지 조회 ( lastId != null )
                queryPersonalChatRoomRepository.findPreviousPersonalChatRoomsByMemberId(
                    ObjectId(queryPersonalChatRoomRequestDto.memberId),
                    ObjectId(queryPersonalChatRoomRequestDto.lastId),
                    queryPersonalChatRoomRequestDto.pageSize,
                )
            }

        // 최신 메시지 정보를 함께 가져와 DTO로 변환
        val queryPersonalChatRoomDTOList =
            personalChatRoomByMemberId.map { personalChatRoom ->
                val latestChatMessage = queryChatMessageService.getLatestChatMessageByChatRoomId(personalChatRoom.chatRoomId)

                QueryPersonalChatRoomResponseDto(
                    chatRoomId = personalChatRoom.chatRoomId,
                    chatRoomName =
                        personalChatRoom.chatRoomName
                            .filterKeys { it != queryPersonalChatRoomRequestDto.memberId } // 현재 memberId를 제외한 값
                            .values
                            .firstOrNull() ?: "알 수 없음",
                    latestMessage = latestChatMessage?.message ?: "채팅 메시지가 없습니다.",
                    latestMessageTime = latestChatMessage?.createdAt ?: personalChatRoom.createdAt ?: LocalDateTime.now(),
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
}