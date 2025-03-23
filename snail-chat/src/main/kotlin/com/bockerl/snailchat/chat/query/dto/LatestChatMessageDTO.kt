package com.bockerl.snailchat.chat.query.dto

import java.time.Instant

data class LatestChatMessageDTO(
    val message: String,
    val createdAt: Instant?,
)