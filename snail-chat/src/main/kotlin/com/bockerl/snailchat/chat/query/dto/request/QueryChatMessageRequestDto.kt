package com.bockerl.snailchat.chat.query.dto.request

data class QueryChatMessageRequestDto(
    val chatRoomId: String,
    val lastId: String?,
    val pageSize: Int,
)