package com.bockerl.snailchat.chat.command.domain.aggregate.vo

data class MemberInfo(
    val memberId: String,
    val memberNickname: String,
    val memberPhoto: String,
)
// ) : Serializable   => 향후 Redis나 Kafka에는 필요