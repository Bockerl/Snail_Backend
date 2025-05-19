package com.bockerl.snailchat.chat.command.domain.aggregate.vo.request

import com.bockerl.snailchat.chat.command.domain.aggregate.enums.ChatMessageType
import com.bockerl.snailchat.chat.command.domain.aggregate.enums.ChatRoomType
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class SendMessageRequestVo(
    @field:Schema(description = "메시지 송신자 아이디", example = "member-0001", type = "String")
    @JsonProperty("memberId")
    val memberId: String,
    @field:Schema(description = "메시지 송신자 닉네임", example = "Alice", type = "String")
    @JsonProperty("memberNickname")
    val memberNickname: String,
    @field:Schema(description = "메시지 송신자 프로필 사진", example = "Alice.jpg", type = "String")
    @JsonProperty("memberPhoto")
    val memberPhoto: String,
    @field:Schema(description = "채팅방 유형", example = "PERSONAL", type = "ChatRoomType")
    @JsonProperty("chatRoomType")
    val chatRoomType: ChatRoomType,
    @field:Schema(description = "메세지 내용", example = "안녕하세요.", type = "String")
    @JsonProperty("message")
    val message: String?, // 처음 입장할 때는 message를 담지 않기 때문에 nullable하게 설정
    @field:Schema(description = "메시지 유형", example = "ENTER", allowableValues = ["ENTER", "CHAT", "LEAVE"])
    @JsonProperty("messageType")
    val messageType: ChatMessageType,
)