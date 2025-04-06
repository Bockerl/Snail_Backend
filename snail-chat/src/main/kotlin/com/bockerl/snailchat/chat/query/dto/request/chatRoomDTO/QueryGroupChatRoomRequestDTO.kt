package com.bockerl.snailchat.chat.query.dto.request.chatRoomDTO

data class QueryGroupChatRoomRequestDTO(
    val memberId: String,
    val lastId: String?,
    val pageSize: Int,
)