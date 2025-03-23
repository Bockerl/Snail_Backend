package com.bockerl.snailchat.chat.query.service

import com.bockerl.snailchat.chat.query.dto.request.QueryGroupChatRoomRequestDTO
import com.bockerl.snailchat.chat.query.dto.request.QueryPersonalChatRoomRequestDTO
import com.bockerl.snailchat.chat.query.dto.response.QueryGroupChatRoomResponseDTO
import com.bockerl.snailchat.chat.query.dto.response.QueryPersonalChatRoomResponseDTO

interface QueryChatRoomService {
    fun getPersonalChatRoomList(queryPersonalChatRoomRequestDto: QueryPersonalChatRoomRequestDTO): List<QueryPersonalChatRoomResponseDTO>

    fun getGroupChatRoomList(queryGroupChatRoomRequestDto: QueryGroupChatRoomRequestDTO): List<QueryGroupChatRoomResponseDTO>
}