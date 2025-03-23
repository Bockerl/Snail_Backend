package com.bockerl.snailchat.chat.query.dto.request

data class QueryPersonalChatRoomRequestDTO(
    val memberId: String,
    val lastId: String?,
    val pageSize: Int,
)