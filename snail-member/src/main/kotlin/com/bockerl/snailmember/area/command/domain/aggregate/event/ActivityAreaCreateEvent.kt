package com.bockerl.snailmember.area.command.domain.aggregate.event

import com.bockerl.snailmember.common.event.BaseActivityAreaEvent
import java.time.Instant

data class ActivityAreaCreateEvent(
    override val memberId: String,
    override val timeStamp: Instant,
    val primaryId: String,
    val workplaceId: String?,
) : BaseActivityAreaEvent