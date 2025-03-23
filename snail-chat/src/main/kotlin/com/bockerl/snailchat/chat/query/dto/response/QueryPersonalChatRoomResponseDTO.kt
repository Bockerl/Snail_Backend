package com.bockerl.snailchat.chat.query.dto.response

import java.time.Instant

data class QueryPersonalChatRoomResponseDTO(
    val chatRoomId: String,
    val chatRoomName: String,
    val chatRoomPhoto: String,
    val latestMessage: String, // 최신 메시지
    val latestMessageTime: Instant?, // 최신 메시지 시간
//    val unreadCount: Int, // 안 읽은 메시지 개수
//    val isMuted: Boolean, // 알림 상태
)