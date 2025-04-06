package com.bockerl.snailchat.chat.query.dto.request.chatMessageDTO

data class QueryChatMessageRequestDTO(
    val chatRoomId: String,
    val lastId: String?,
    val pageSize: Int,
)