package com.bockerl.snailmember.member.command.domain.aggregate.event

import com.bockerl.snailmember.common.event.BaseMemberEvent
import java.time.Instant

data class MemberLoginEvent(
    override val memberId: String,
    override val timestamp: Instant,
    val ipAddress: String,
    val userAgent: String,
) : BaseMemberEvent