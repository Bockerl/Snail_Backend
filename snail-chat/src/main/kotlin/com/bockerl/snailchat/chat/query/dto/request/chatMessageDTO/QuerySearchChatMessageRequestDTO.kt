package com.bockerl.snailchat.chat.query.dto.request.chatMessageDTO

data class QuerySearchChatMessageRequestDTO(
    val chatRoomId: String,
    val keyword: String,
    val page: Int = 0, // 현재 페이지 (0부터 시작)
    val pageSize: Int = 20, // 한 페이지당 반환할 결과 건수
)