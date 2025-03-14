package com.bockerl.snailchat.chat.command.domain.aggregate.vo.request

import com.bockerl.snailchat.chat.command.domain.aggregate.enums.ChatRoomType
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class CommandGroupChatRoomCreateRequestVo(
    @field:Schema(description = "채팅방 타입", example = "PERSONAL", allowableValues = ["PERSONAL", "GROUP"])
    @JsonProperty("chatRoomType")
    val chatRoomType: ChatRoomType,
)