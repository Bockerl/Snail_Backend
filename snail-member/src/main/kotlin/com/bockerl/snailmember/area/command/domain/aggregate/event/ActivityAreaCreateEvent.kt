package com.bockerl.snailmember.area.command.domain.aggregate.event

import com.bockerl.snailmember.common.event.BaseActivityAreaEvent
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class ActivityAreaCreateEvent(
    override val memberId: String,
    override val timeStamp: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC),
    val primaryId: String,
    val workplaceId: String?,
) : BaseActivityAreaEvent