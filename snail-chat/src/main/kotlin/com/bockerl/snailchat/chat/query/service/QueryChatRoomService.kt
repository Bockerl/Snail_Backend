package com.bockerl.snailchat.chat.query.service

import com.bockerl.snailchat.chat.query.dto.request.QueryPersonalChatRoomRequestDto
import com.bockerl.snailchat.chat.query.dto.response.QueryPersonalChatRoomResponseDto

interface QueryChatRoomService {
    fun getPersonalChatRoomList(queryPersonalChatRoomRequestDto: QueryPersonalChatRoomRequestDto): List<QueryPersonalChatRoomResponseDto>
}