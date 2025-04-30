package com.bockerl.snailmember.common.event

import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberCreateEvent
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberDeleteEvent
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberLoginEvent
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberUpdateEvent
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.OffsetDateTime

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "eventType",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = MemberCreateEvent::class, name = "MEMBER_CREATE"),
    JsonSubTypes.Type(value = MemberUpdateEvent::class, name = "MEMBER_UPDATE"),
    JsonSubTypes.Type(value = MemberDeleteEvent::class, name = "MEMBER_DELETE"),
    JsonSubTypes.Type(value = MemberLoginEvent::class, name = "MEMBER_LOGIN"),
)
interface BaseMemberEvent {
    val memberId: String
    val timestamp: OffsetDateTime
}