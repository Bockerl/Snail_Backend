package com.bockerl.snailchat.chat.command.application.dto.request

import com.bockerl.snailchat.chat.command.domain.aggregate.enums.CommandChatMessageType
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class CommandChatMessageRequestDto(
    @field:Schema(description = "채팅방 번호", example = "1", type = "String")
    @JsonProperty("chatRoomId")
    val chatRoomId: String, // Dto는 데이터 전달하는 역할임으로, val(불변객체)로 선언
    @field:Schema(description = "메시지 송신자", example = "Alice", type = "String")
    @JsonProperty("sender")
    val sender: String,
    @field:Schema(description = "메세지 내용", example = "안녕하세요.", type = "String")
    @JsonProperty("message")
    val message: String?, // 처음 입장할 때는 message를 담지 않기 때문에 nullable하게 설정
    @field:Schema(description = "메시지 유형", example = "ENTER", allowableValues = ["ENTER", "CHAT", "LEAVE"])
    @JsonProperty("messageType")
    val messageType: CommandChatMessageType,
)