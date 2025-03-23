package com.bockerl.snailchat.chat.query.dto.request

data class QueryGroupChatRoomRequestDTO(
    val memberId: String,
    val lastId: String?,
    val pageSize: Int,
)