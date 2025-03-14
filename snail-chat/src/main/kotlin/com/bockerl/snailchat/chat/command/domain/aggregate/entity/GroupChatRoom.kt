package com.bockerl.snailchat.chat.command.domain.aggregate.entity

import com.bockerl.snailchat.chat.command.domain.aggregate.enums.ChatRoomType
import com.bockerl.snailchat.chat.command.domain.aggregate.vo.MemberInfo
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "GroupChatRoom")
data class GroupChatRoom(
    @Id
    val chatRoomId: ObjectId = ObjectId.get(),
    val chatRoomName: String?, // 그룹 채팅방은 모임 이름
    val chatRoomType: ChatRoomType,
    val chatRoomCategory: String,
    val chatRoomStatus: Boolean,
    val participants: List<MemberInfo>,
    val participantsNum: Int,
    @CreatedDate
    var createdAt: LocalDateTime? = null,
    // 그룹 채팅방 메인 & 서브 고려 필요 - 따로 Entity를 뺼지 고민 필요
    // val chatRoomRelation: CommandCHatRoomRelationType? = null
    // 알람 여부 - 향후 알림 구현시 추가 예정
    // val chatRoomAlarmStatus: Boolean,
)