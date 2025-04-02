package com.bockerl.snailchat.infrastructure.outbox.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "이벤트 타입", enumAsRef = true)
enum class EventType {
    @Schema(description = "메시지 전송")
    MESSAGE_SENT,
}