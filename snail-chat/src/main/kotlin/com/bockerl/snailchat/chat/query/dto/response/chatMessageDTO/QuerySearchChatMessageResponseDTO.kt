package com.bockerl.snailchat.chat.query.dto.response.chatMessageDTO

data class QuerySearchChatMessageResponseDTO(
    val messages: List<QueryChatMessageResponseDTO>, // 메시지 리스트
    val page: Int, // 현재 페이지 번호
    val pageSize: Int, // 페이지 당 개수
    val totalCount: Long?, // 전체 검색 결과 개수
    val hasNext: Boolean, // 다음 페이지 여부
    val startIndex: Int, // 전체 결과 중 현재 페이지의 첫 번째 메시지 순번
)