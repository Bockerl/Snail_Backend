package com.bockerl.snailchat.chat.query.service

import com.bockerl.snailchat.chat.command.domain.aggregate.entity.ChatMessage
import com.bockerl.snailchat.chat.query.dto.request.QueryChatMessageRequestDto
import com.bockerl.snailchat.chat.query.dto.response.QueryChatMessageResponseDto
import com.bockerl.snailchat.chat.query.mapper.EntityToDtoConverter
import com.bockerl.snailchat.chat.query.repository.QueryChatMessageRepository
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
}