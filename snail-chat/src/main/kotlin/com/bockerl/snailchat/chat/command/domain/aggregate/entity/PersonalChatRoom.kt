package com.bockerl.snailchat.chat.command.domain.aggregate.entity

import com.bockerl.snailchat.chat.command.domain.aggregate.enums.ChatRoomType
import com.bockerl.snailchat.chat.command.domain.aggregate.vo.MemberInfo
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "PersonalChatRoom")
data class PersonalChatRoom(
    @Id
    val chatRoomId: ObjectId = ObjectId.get(),
    val chatRoomName: Map<String, String>, // { "userA": "John", "userB": "Alice" } - 각자 볼 때 채팅방 이름 다르도록
    val chatRoomPhoto: Map<String, String>, // { "userA": "John.jpg", "userB": "Alice.jpg" } - 각자 볼 때 채팅방 사진 다르도록
    val chatRoomType: ChatRoomType,
    val chatRoomStatus: Boolean,
    val participants: List<MemberInfo>,
    @CreatedDate
    var createdAt: Instant? = null,
    @LastModifiedDate
    var updatedAt: Instant? = null,
    // 알람 여부 - 향후 알림 구현시 추가 예정
    // val chatRoomAlarmStatus: Boolean,
) : Persistable<ObjectId> {
    override fun getId(): ObjectId = chatRoomId

    // createdAt이 없으면 신규 객체로 간주하여 @CreatedDate가 적용되도록 설정
    override fun isNew(): Boolean = createdAt == null
}