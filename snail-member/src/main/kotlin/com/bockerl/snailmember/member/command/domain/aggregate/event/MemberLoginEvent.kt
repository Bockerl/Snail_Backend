package com.bockerl.snailmember.member.command.domain.aggregate.event

import com.bockerl.snailmember.common.event.BaseMemberEvent
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class MemberLoginEvent(
    override val memberId: String,
    override val timestamp: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC),
    val ipAddress: String,
    val userAgent: String,
    val idemPotencyKey: String,
) : BaseMemberEvent