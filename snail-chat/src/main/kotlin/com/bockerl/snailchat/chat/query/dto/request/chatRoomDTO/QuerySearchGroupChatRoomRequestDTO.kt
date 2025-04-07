package com.bockerl.snailchat.chat.query.dto.request.chatRoomDTO

data class QuerySearchGroupChatRoomRequestDTO(
    val memberId: String,
    val keyword: String,
    val limit: Int = 10,
)