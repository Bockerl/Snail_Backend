package com.bockerl.snailmember.gathering.command.application.dto

import com.bockerl.snailmember.gathering.command.domain.enums.GatheringType

class CommandGatheringUpdateDTO(
    var gatheringId: String,
    var gatheringInformation: String?,
    var gatheringType: GatheringType,
    var gatheringRegion: String,
    var gatheringLimit: Int,
    val memberId: String,
    val deleteFilesIds: List<Long>,
    val idempotencyKey: String,
)