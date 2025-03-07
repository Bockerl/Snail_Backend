package com.bockerl.snailchat.chat.command.domain.aggregate.vo.request

import com.bockerl.snailchat.chat.command.domain.aggregate.enums.CommandChatRoomType
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class CommandChatRoomCreateRequestVo(
    @field:Schema(description = "채팅방 이름(개인 채팅방인 경우 sender의 nickName)", example = "Alice", type = "String")
    @JsonProperty("chatRoomName")
    val chatRoomName: String?,
    @field:Schema(description = "채팅방 타입", example = "PERSONAL", allowableValues = ["PERSONAL", "GROUP"])
    @JsonProperty("chatRoomType")
    val chatRoomType: CommandChatRoomType,
)