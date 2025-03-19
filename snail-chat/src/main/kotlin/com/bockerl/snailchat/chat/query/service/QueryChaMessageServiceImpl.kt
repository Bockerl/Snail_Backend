package com.bockerl.snailchat.chat.query.service

import com.bockerl.snailchat.chat.command.domain.aggregate.entity.ChatMessage
import com.bockerl.snailchat.chat.command.domain.aggregate.enums.ChatMessageType
import com.bockerl.snailchat.chat.query.dto.LatestChatMessageDto
import com.bockerl.snailchat.chat.query.dto.request.QueryChatMessageRequestDto
import com.bockerl.snailchat.chat.query.dto.response.QueryChatMessageResponseDto
import com.bockerl.snailchat.chat.query.mapper.EntityToDtoConverter
import com.bockerl.snailchat.chat.query.repository.queryChatMessage.QueryChatMessageRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class QueryChaMessageServiceImpl(
    private val queryChatMessageRepository: QueryChatMessageRepository,
    private val entityToDtoConverter: EntityToDtoConverter,
) : QueryChatMessageService {
    override fun getChatMessageByChatRoomId(queryChatMessageRequestDto: QueryChatMessageRequestDto): List<QueryChatMessageResponseDto> {
        val chatMessagesByChatRoomId: List<ChatMessage> =
            // 첫 페이지 메시지 조회 ( lastId = null )
            if (queryChatMessageRequestDto.lastId == null) {
                queryChatMessageRepository.findLatestChatMessagesByChatRoomId(
                    ObjectId(queryChatMessageRequestDto.chatRoomId),
                    queryChatMessageRequestDto.pageSize,
                )
            } else {
                // 다음 페이지 메시지 조회 ( lastId != null )
                queryChatMessageRepository.findPreviousChatMessagesByChatRoomId(
                    ObjectId(queryChatMessageRequestDto.chatRoomId),
                    ObjectId(queryChatMessageRequestDto.lastId),
                    queryChatMessageRequestDto.pageSize,
                )
            }

        return chatMessagesByChatRoomId.map { entityToDtoConverter.chatMessageToQueryChatMessageResponseDto(it) }
    }

    override fun getIsFirstJoin(
        chatRoomId: String,
        memberId: String,
    ): Boolean {
        // 해당 user의 LEAVE 상태로 변경된 가장 최근의 시간 조회
        val lastLeaveMessage: ChatMessage? =
            queryChatMessageRepository
                .findTopByChatRoomIdAndMemberIdAndMessageTypeOrderByCreatedAtDesc(ObjectId(chatRoomId), memberId, ChatMessageType.LEAVE)

        // 해당 member의 ENTER 상태로 가장 최근의 시간 조회
        val lastEnterMessage: ChatMessage? =
            queryChatMessageRepository
                .findTopByChatRoomIdAndMemberIdAndMessageTypeOrderByCreatedAtDesc(ObjectId(chatRoomId), memberId, ChatMessageType.ENTER)

        // LEAVE가 없을 경우에는 최초 입장
        if (lastLeaveMessage == null) {
            return true
        }

        // LEAVE는 있는데 ENTER는 없거나(에러), LEAVE가 ENTER보다 더 최근이면, 최초 입장으로 처리 (나갔다가 다시 들어옴)
        return lastEnterMessage == null || (lastLeaveMessage.createdAt?.isAfter(lastEnterMessage.createdAt) ?: false)
    }

    override fun getLatestChatMessageByChatRoomId(chatRoomId: ObjectId): LatestChatMessageDto? {
        val latestChatMessage =
            queryChatMessageRepository.findTopByChatRoomIdOrderByCreatedAtDesc(chatRoomId)

        return latestChatMessage?.let { entityToDtoConverter.latestChatMessageToLatestChatMessageDto(it) }
    }
}