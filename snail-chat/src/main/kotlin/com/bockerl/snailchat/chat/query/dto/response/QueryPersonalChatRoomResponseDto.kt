package com.bockerl.snailchat.chat.query.dto.response

import org.bson.types.ObjectId
import java.time.LocalDateTime

data class QueryPersonalChatRoomResponseDto(
    val chatRoomId: ObjectId,
    val chatRoomName: String?,
    val latestMessage: String, // 최신 메시지
    val latestMessageTime: LocalDateTime, // 최신 메시지 시간
//    val unreadCount: Int, // 안 읽은 메시지 개수
//    val isMuted: Boolean, // 알림 상태
)