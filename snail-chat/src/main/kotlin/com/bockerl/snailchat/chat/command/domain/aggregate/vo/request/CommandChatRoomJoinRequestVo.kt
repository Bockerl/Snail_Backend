package com.bockerl.snailchat.chat.command.domain.aggregate.vo.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class CommandChatRoomJoinRequestVo(
    @field:Schema(description = "채팅방 번호", example = "67da71569daacf3824f56bef", type = "String")
    @JsonProperty("chatRoomId")
    val chatRoomId: String, // Dto는 데이터 전달하는 역할임으로, val(불변객체)로 선언
)