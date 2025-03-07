package com.bockerl.snailchat.chat.command.application.dto.request

import com.bockerl.snailchat.chat.command.domain.aggregate.enums.CommandChatRoomType

data class CommandChatRoomCreateRequestDto(
    val chatRoomName: String?,
    val chatRoomType: CommandChatRoomType,
//    val creator: MemberInfo,
//    val participants: List<MemberInfo>,
)