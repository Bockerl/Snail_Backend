package com.bockerl.snailmember.member.command.domain.aggregate.event

import com.bockerl.snailmember.common.event.BaseMemberEvent
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class MemberDeleteEvent(
    override val memberId: String,
    override val timestamp: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC),
    val memberEmail: String,
) : BaseMemberEvent