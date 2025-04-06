package com.bockerl.snailmember.gathering.command.application.dto

import com.bockerl.snailmember.gathering.command.domain.enums.GatheringType

data class CommandGatheringCreateDTO(
    var gatheringTitle: String?,
    var gatheringInformation: String?,
    var gatheringType: GatheringType,
    var gatheringRegion: String,
    var gatheringLimit: Int,
    val memberId: String,
    val idempotencyKey: String,
)