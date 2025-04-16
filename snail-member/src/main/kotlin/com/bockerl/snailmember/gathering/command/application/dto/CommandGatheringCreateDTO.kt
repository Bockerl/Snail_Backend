package com.bockerl.snailmember.gathering.command.application.dto

import com.bockerl.snailmember.gathering.command.domain.enums.GatheringType

data class CommandGatheringCreateDTO(
    val gatheringTitle: String?,
    val gatheringInformation: String?,
    val gatheringType: GatheringType,
    val gatheringRegion: String,
    val gatheringLimit: Int,
    val memberId: String,
    val idempotencyKey: String,
)