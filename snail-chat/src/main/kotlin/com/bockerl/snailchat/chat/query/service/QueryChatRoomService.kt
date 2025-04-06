package com.bockerl.snailchat.chat.query.service

import com.bockerl.snailchat.chat.query.dto.request.chatRoomDTO.QueryGroupChatRoomRequestDTO
import com.bockerl.snailchat.chat.query.dto.request.chatRoomDTO.QueryPersonalChatRoomRequestDTO
import com.bockerl.snailchat.chat.query.dto.response.chatRoomDTO.QueryGroupChatRoomResponseDTO
import com.bockerl.snailchat.chat.query.dto.response.chatRoomDTO.QueryPersonalChatRoomResponseDTO

interface QueryChatRoomService {
    fun getPersonalChatRoomList(queryPersonalChatRoomRequestDto: QueryPersonalChatRoomRequestDTO): List<QueryPersonalChatRoomResponseDTO>

    fun getGroupChatRoomList(queryGroupChatRoomRequestDto: QueryGroupChatRoomRequestDTO): List<QueryGroupChatRoomResponseDTO>
}