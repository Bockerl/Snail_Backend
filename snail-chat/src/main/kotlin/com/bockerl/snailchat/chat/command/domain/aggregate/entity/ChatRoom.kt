package com.bockerl.snailchat.chat.command.domain.aggregate.entity

import com.bockerl.snailchat.chat.command.domain.aggregate.enums.CommandChatRoomType
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import java.time.LocalDateTime

data class ChatRoom(
    @Id
    val chatRoomId: String? = null,
    val chatRoomName: String?,
    val chatRoomType: CommandChatRoomType,
    val chatRoomStatus: Boolean,
    // 그룹 채팅방 메인 & 서브 고려 필요 - 따로 Entity를 뺼지 고민 필요
    // val chatRoomRelation: CommandCHatRoomRelationType? = null
    // 알람 여부 - 향후 알림 구현시 추가 예정
    // val chatRoomAlarmStatus: Boolean,
    val participants: List<MemberInfo>,
    @CreatedDate
    var createdAt: LocalDateTime? = null,
)

data class MemberInfo(
    val memberId: String,
    val memberNickname: String,
    val memberPhoto: String? = "",
)