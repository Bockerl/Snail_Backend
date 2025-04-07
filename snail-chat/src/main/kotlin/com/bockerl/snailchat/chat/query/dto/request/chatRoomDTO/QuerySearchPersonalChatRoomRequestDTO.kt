package com.bockerl.snailchat.chat.query.dto.request.chatRoomDTO

data class QuerySearchPersonalChatRoomRequestDTO(
    val memberId: String,
    val keyword: String,
    val limit: Int = 10,
)