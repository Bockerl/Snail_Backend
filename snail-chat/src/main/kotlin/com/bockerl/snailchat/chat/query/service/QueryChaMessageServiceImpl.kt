package com.bockerl.snailchat.chat.query.service

import com.bockerl.snailchat.chat.command.domain.aggregate.entity.ChatMessage
import com.bockerl.snailchat.chat.command.domain.aggregate.enums.ChatMessageType
import com.bockerl.snailchat.chat.query.dto.LatestChatMessageDTO
import com.bockerl.snailchat.chat.query.dto.request.chatMessageDTO.QueryChatMessageRequestDTO
import com.bockerl.snailchat.chat.query.dto.request.chatMessageDTO.QuerySearchChatMessageRequestDTO
import com.bockerl.snailchat.chat.query.dto.response.chatMessageDTO.QueryChatMessageResponseDTO
import com.bockerl.snailchat.chat.query.dto.response.chatMessageDTO.QuerySearchChatMessageResponseDTO
import com.bockerl.snailchat.chat.query.mapper.EntityToDtoConverter
import com.bockerl.snailchat.chat.query.repository.queryChatMessage.QueryChatMessageRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class QueryChaMessageServiceImpl(
    private val queryChatMessageRepository: QueryChatMessageRepository,
    private val entityToDtoConverter: EntityToDtoConverter,
) : QueryChatMessageService {
    @Transactional
    override fun getChatMessageByChatRoomId(queryChatMessageRequestDTO: QueryChatMessageRequestDTO): List<QueryChatMessageResponseDTO> {
        val chatMessagesByChatRoomId: List<ChatMessage> =
            // 첫 페이지 메시지 조회 ( lastId = null )
            if (queryChatMessageRequestDTO.lastId == null) {
                queryChatMessageRepository.findLatestChatMessagesByChatRoomId(
                    ObjectId(queryChatMessageRequestDTO.chatRoomId),
                    queryChatMessageRequestDTO.pageSize,
                )
            } else {
                // 다음 페이지 메시지 조회 ( lastId != null )
                queryChatMessageRepository.findPreviousChatMessagesByChatRoomId(
                    ObjectId(queryChatMessageRequestDTO.chatRoomId),
                    ObjectId(queryChatMessageRequestDTO.lastId),
                    queryChatMessageRequestDTO.pageSize,
                )
            }

        return chatMessagesByChatRoomId.map { entityToDtoConverter.chatMessageToQueryChatMessageResponseDTO(it) }
    }

    @Transactional
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

    @Transactional
    override fun getLatestChatMessageByChatRoomId(chatRoomId: ObjectId): LatestChatMessageDTO? {
        val latestChatMessage =
            queryChatMessageRepository.findTopByChatRoomIdOrderByCreatedAtDesc(chatRoomId)

        return latestChatMessage?.let { entityToDtoConverter.latestChatMessageToLatestChatMessageDTO(it) }
    }

    @Transactional
    override fun searchChatMessageByKeyword(
        querySearchChatMessageRequestDTO: QuerySearchChatMessageRequestDTO,
    ): QuerySearchChatMessageResponseDTO {
        val chatRoomId = ObjectId(querySearchChatMessageRequestDTO.chatRoomId)

        // 키워드가 공백이면 빈 결과 반환
        if (querySearchChatMessageRequestDTO.keyword.isBlank()) {
            return QuerySearchChatMessageResponseDTO(
                messages = emptyList(),
                page = querySearchChatMessageRequestDTO.page,
                pageSize = querySearchChatMessageRequestDTO.pageSize,
                totalCount = 0,
                hasNext = false,
                startIndex = 0,
            )
        }

        val chatMessagesByKeyword: List<ChatMessage> =
            queryChatMessageRepository.findChatMessagesByChatRoomIdAndMessageContainingKeyword(
                chatRoomId,
                querySearchChatMessageRequestDTO.keyword,
                querySearchChatMessageRequestDTO.page,
                querySearchChatMessageRequestDTO.pageSize,
            )

        val messageDto: List<QueryChatMessageResponseDTO> =
            chatMessagesByKeyword.map { entityToDtoConverter.chatMessageToQueryChatMessageResponseDTO(it) }

        val totalCount =
            queryChatMessageRepository.countChatMessagesByChatRoomIdAndMessageContainingKeyword(
                chatRoomId,
                querySearchChatMessageRequestDTO.keyword,
            )

        // 현재 페이지의 시작 순번 계산 (1부터 시작)
        val startIndex = querySearchChatMessageRequestDTO.page * querySearchChatMessageRequestDTO.pageSize + 1

        // 다음 페이지 여부 계산
        val hasNext = (querySearchChatMessageRequestDTO.page + 1) * querySearchChatMessageRequestDTO.pageSize < totalCount

        return QuerySearchChatMessageResponseDTO(
            messages = messageDto,
            page = querySearchChatMessageRequestDTO.page,
            pageSize = querySearchChatMessageRequestDTO.pageSize,
            totalCount = totalCount,
            hasNext = hasNext,
            startIndex = startIndex,
        )
    }
}