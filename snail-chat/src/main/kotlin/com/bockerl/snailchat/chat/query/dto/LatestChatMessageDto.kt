package com.bockerl.snailchat.chat.query.dto

import java.time.LocalDateTime

data class LatestChatMessageDto(
    val message: String,
    val createdAt: LocalDateTime,
)