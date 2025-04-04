package com.bockerl.snailmember.member.command.domain.aggregate.event

import com.bockerl.snailmember.common.event.BaseMemberEvent
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.MemberActionType
import java.time.Instant

data class MemberEvent(
    override val memberId: String,
    override val timestamp: Instant = Instant.now(),
    val
    val memberActionType: MemberActionType,
) : BaseMemberEvent