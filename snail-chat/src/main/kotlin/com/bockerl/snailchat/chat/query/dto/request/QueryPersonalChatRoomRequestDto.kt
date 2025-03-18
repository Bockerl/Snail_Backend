package com.bockerl.snailchat.chat.query.dto.request

data class QueryPersonalChatRoomRequestDto(
    val memberId: String,
    val lastId: String?,
    val pageSize: Int,
)