package com.bockerl.snailmember.common.event

import com.bockerl.snailmember.area.command.domain.aggregate.event.ActivityAreaCreateEvent
import com.bockerl.snailmember.area.command.domain.aggregate.event.ActivityAreaUpdateEvent
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.Instant

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "eventType",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ActivityAreaCreateEvent::class, name = "ACTIVITY_AREA_CREATE"),
    JsonSubTypes.Type(value = ActivityAreaUpdateEvent::class, name = "ACTIVITY_AREA_UPDATE"),
)
interface BaseActivityAreaEvent {
    val memberId: String
    val timeStamp: Instant
}