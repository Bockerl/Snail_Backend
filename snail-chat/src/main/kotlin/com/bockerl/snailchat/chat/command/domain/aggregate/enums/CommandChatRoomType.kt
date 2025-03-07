package com.bockerl.snailchat.chat.command.domain.aggregate.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "채팅방 타입", enumAsRef = true)
enum class CommandChatRoomType {
    @Schema(description = "개인 채팅방")
    PERSONAL,

    @Schema(description = "그룹 채팅방")
    GROUP,
}