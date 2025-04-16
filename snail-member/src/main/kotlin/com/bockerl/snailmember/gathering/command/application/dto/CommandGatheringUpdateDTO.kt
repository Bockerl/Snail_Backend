package com.bockerl.snailmember.gathering.command.application.dto

import com.bockerl.snailmember.gathering.command.domain.enums.GatheringType

class CommandGatheringUpdateDTO(
    val gatheringId: String,
    val gatheringInformation: String?,
    val gatheringType: GatheringType,
    val gatheringRegion: String,
    val gatheringLimit: Int,
    val memberId: String,
    val deleteFilesIds: List<Long>,
    val idempotencyKey: String,
)