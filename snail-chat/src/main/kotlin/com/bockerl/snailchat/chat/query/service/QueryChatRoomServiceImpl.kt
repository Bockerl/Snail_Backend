package com.bockerl.snailchat.chat.query.service

import com.bockerl.snailchat.chat.query.dto.request.chatRoomDTO.QueryGroupChatRoomRequestDTO
import com.bockerl.snailchat.chat.query.dto.request.chatRoomDTO.QueryPersonalChatRoomRequestDTO
import com.bockerl.snailchat.chat.query.dto.request.chatRoomDTO.QuerySearchGroupChatRoomRequestDTO
import com.bockerl.snailchat.chat.query.dto.request.chatRoomDTO.QuerySearchPersonalChatRoomRequestDTO
import com.bockerl.snailchat.chat.query.dto.response.chatRoomDTO.QueryGroupChatRoomResponseDTO
import com.bockerl.snailchat.chat.query.dto.response.chatRoomDTO.QueryPersonalChatRoomResponseDTO
import com.bockerl.snailchat.chat.query.repository.queryGroupChatRoom.QueryGroupChatRoomRepository
import com.bockerl.snailchat.chat.query.repository.queryPersonalChatRoom.QueryPersonalChatRoomRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class QueryChatRoomServiceImpl(
    private val queryPersonalChatRoomRepository: QueryPersonalChatRoomRepository,
    private val queryGroupChatRoomRepository: QueryGroupChatRoomRepository,
    private val queryChatMessageService: QueryChatMessageService,
) : QueryChatRoomService {
    @Transactional
    override fun getPersonalChatRoomList(
        queryPersonalChatRoomRequestDTO: QueryPersonalChatRoomRequestDTO,
    ): List<QueryPersonalChatRoomResponseDTO> {
        val personalChatRoomByMemberId =
            if (queryPersonalChatRoomRequestDTO.lastId == null) {
                queryPersonalChatRoomRepository.findLatestPersonalChatRoomsByMemberId(
                    queryPersonalChatRoomRequestDTO.memberId,
                    queryPersonalChatRoomRequestDTO.pageSize,
                )
            } else {
                // 다음 페이지 메시지 조회 ( lastId != null )
                queryPersonalChatRoomRepository.findPreviousPersonalChatRoomsByMemberId(
                    queryPersonalChatRoomRequestDTO.memberId,
                    ObjectId(queryPersonalChatRoomRequestDTO.lastId),
                    queryPersonalChatRoomRequestDTO.pageSize,
                )
            }

        // 최신 메시지 정보를 함께 가져와 DTO로 변환
        return personalChatRoomByMemberId.map { personalChatRoom ->
            val latestChatMessage = queryChatMessageService.getLatestChatMessageByChatRoomId(personalChatRoom.chatRoomId)

            QueryPersonalChatRoomResponseDTO(
                chatRoomId = personalChatRoom.chatRoomId.toString(),
                chatRoomName =
                    personalChatRoom.chatRoomName
                        .filterKeys { it == queryPersonalChatRoomRequestDTO.memberId } // 현재 memberId를 제외한 값
                        .values
                        .firstOrNull() ?: "알 수 없음",
                chatRoomPhoto =
                    personalChatRoom.chatRoomPhoto
                        .filterKeys { it == queryPersonalChatRoomRequestDTO.memberId } // 현재 memberId를 제외한 값
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
    }

    @Transactional
    override fun getGroupChatRoomList(queryGroupChatRoomRequestDTO: QueryGroupChatRoomRequestDTO): List<QueryGroupChatRoomResponseDTO> {
        val groupChatRoomByMemberId =
            if (queryGroupChatRoomRequestDTO.lastId == null) {
                queryGroupChatRoomRepository.findLatestGroupChatRoomsByMemberId(
                    queryGroupChatRoomRequestDTO.memberId,
                    queryGroupChatRoomRequestDTO.pageSize,
                )
            } else {
                // 다음 페이지 메시지 조회 ( lastId != null )
                queryGroupChatRoomRepository.findPreviousGroupChatRoomsByMemberId(
                    queryGroupChatRoomRequestDTO.memberId,
                    ObjectId(queryGroupChatRoomRequestDTO.lastId),
                    queryGroupChatRoomRequestDTO.pageSize,
                )
            }

        // 최신 메시지 정보를 함께 가져와 DTO로 변환
        return groupChatRoomByMemberId.map { groupChatRoom ->
            val latestChatMessage = queryChatMessageService.getLatestChatMessageByChatRoomId(groupChatRoom.chatRoomId)

            QueryGroupChatRoomResponseDTO(
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
    }

    @Transactional
    override fun searchPersonalChatRoomByKeyword(
        querySearchPersonalChatRoomDTO: QuerySearchPersonalChatRoomRequestDTO,
    ): List<QueryPersonalChatRoomResponseDTO> {
        val personalChatRoomsByKeyword =
            queryPersonalChatRoomRepository.findPersonalChatRoomsByMemberIdAndChatRoomNameContainingKeyword(
                memberId = querySearchPersonalChatRoomDTO.memberId,
                keyword = querySearchPersonalChatRoomDTO.keyword,
                limit = querySearchPersonalChatRoomDTO.limit,
            )

        println(personalChatRoomsByKeyword)

        return personalChatRoomsByKeyword.map { chatRoom ->
            val latestMessage = queryChatMessageService.getLatestChatMessageByChatRoomId(chatRoom.chatRoomId)

            QueryPersonalChatRoomResponseDTO(
                chatRoomId = chatRoom.chatRoomId.toHexString(),
                chatRoomName =
                    chatRoom.chatRoomName
                        .filterKeys { it == querySearchPersonalChatRoomDTO.memberId } // 현재 memberId를 제외한 값
                        .values
                        .firstOrNull() ?: "알 수 없음",
                chatRoomPhoto =
                    chatRoom.chatRoomPhoto
                        .filterKeys { it == querySearchPersonalChatRoomDTO.memberId } // 현재 memberId를 제외한 값
                        .values
                        .firstOrNull() ?: "알 수 없음",
                latestMessage = latestMessage?.message ?: "채팅 메시지가 없습니다.",
                latestMessageTime = latestMessage?.createdAt ?: chatRoom.createdAt ?: Instant.now(),
//                    unreadCount =
//                        getUnreadMessageCount(
//                            personalChatRoom.chatRoomId,
//                            ObjectId(queryPersonalChatRoomRequestDto.memberId),
//                        ),
//                    isMuted = isChatRoomMuted(chatRoom.chatRoomId, ObjectId(queryPersonalChatRoomRequestDto.memberId)),
            )
        }
    }

    @Transactional
    override fun searchGroupChatRoomByKeyword(
        querySearchGroupChatRoomDTO: QuerySearchGroupChatRoomRequestDTO,
    ): List<QueryGroupChatRoomResponseDTO> {
        val groupChatRoomsByKeyword =
            queryGroupChatRoomRepository.findGroupChatRoomsByMemberIdAndChatRoomNameContainingKeyword(
                memberId = querySearchGroupChatRoomDTO.memberId,
                keyword = querySearchGroupChatRoomDTO.keyword,
                limit = querySearchGroupChatRoomDTO.limit,
            )

        // 최신 메시지 정보를 함께 가져와 DTO로 변환
        return groupChatRoomsByKeyword.map { groupChatRoom ->

            val latestChatMessage = queryChatMessageService.getLatestChatMessageByChatRoomId(groupChatRoom.chatRoomId)

            QueryGroupChatRoomResponseDTO(
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
    }

    override fun getGroupChatRoomParticipantsNum(chatRoomId: ObjectId): Int =
        queryGroupChatRoomRepository
            .findById(chatRoomId)
            .orElseThrow { IllegalArgumentException("그룹 채팅방 없음") }
            .participantsNum
}