package com.bockerl.snailchat.chat.command.domain.aggregate.vo.request

import com.bockerl.snailchat.chat.command.domain.aggregate.enums.CommandChatMessageType
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class SendMessageRequestVo(
    @field:Schema(description = "채팅방")
    @JsonProperty("chatRoomId")
    val chatRoomId: String,
    @field:Schema(description = "송신자")
    @JsonProperty("sender")
    val sender: String,
    @field:Schema(description = "메시지")
    @JsonProperty("message")
    val message: String?,
    @field:Schema(description = "메시지 타입")
    @JsonProperty("messageType")
    val messageType: CommandChatMessageType,
)