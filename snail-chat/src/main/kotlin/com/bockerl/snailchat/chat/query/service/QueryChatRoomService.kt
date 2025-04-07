package com.bockerl.snailchat.chat.query.service

import com.bockerl.snailchat.chat.query.dto.request.chatRoomDTO.QueryGroupChatRoomRequestDTO
import com.bockerl.snailchat.chat.query.dto.request.chatRoomDTO.QueryPersonalChatRoomRequestDTO
import com.bockerl.snailchat.chat.query.dto.request.chatRoomDTO.QuerySearchGroupChatRoomRequestDTO
import com.bockerl.snailchat.chat.query.dto.request.chatRoomDTO.QuerySearchPersonalChatRoomRequestDTO
import com.bockerl.snailchat.chat.query.dto.response.chatRoomDTO.QueryGroupChatRoomResponseDTO
import com.bockerl.snailchat.chat.query.dto.response.chatRoomDTO.QueryPersonalChatRoomResponseDTO

interface QueryChatRoomService {
    fun getPersonalChatRoomList(queryPersonalChatRoomRequestDTO: QueryPersonalChatRoomRequestDTO): List<QueryPersonalChatRoomResponseDTO>

    fun getGroupChatRoomList(queryGroupChatRoomRequestDTO: QueryGroupChatRoomRequestDTO): List<QueryGroupChatRoomResponseDTO>

    fun searchPersonalChatRoomByKeyword(
        querySearchPersonalChatRoomDTO: QuerySearchPersonalChatRoomRequestDTO,
    ): List<QueryPersonalChatRoomResponseDTO>

    fun searchGroupChatRoomByKeyword(querySearchGroupChatRoomDTO: QuerySearchGroupChatRoomRequestDTO): List<QueryGroupChatRoomResponseDTO>
}