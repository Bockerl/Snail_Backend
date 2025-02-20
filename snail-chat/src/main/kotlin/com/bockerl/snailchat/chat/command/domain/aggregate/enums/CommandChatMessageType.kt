package com.bockerl.snailchat.chat.command.domain.aggregate.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "메시지 타입", enumAsRef = true)
enum class CommandChatMessageType {
    @Schema(description = "채팅방 입장")
    ENTER,

    @Schema(description = "채팅")
    CHAT,

    @Schema(description = "채팅방 퇴장")
    LEAVE,
}