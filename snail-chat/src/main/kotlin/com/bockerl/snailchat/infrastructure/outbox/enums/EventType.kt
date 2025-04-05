package com.bockerl.snailchat.infrastructure.outbox.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "이벤트 타입", enumAsRef = true)
enum class EventType(
    val topic: String,
) {
    @Schema(description = "개인 채팅방메시지 전송")
    PERSONAL_MESSAGE_SENT("personal-chat-message-topic"),

    @Schema(description = "그룹 채팅방 메시지 전송")
    GROUP_MESSAGE_SENT("group-chat-message-topic"),
}