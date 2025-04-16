package com.bockerl.snailmember.gathering.command.application.dto

import com.bockerl.snailmember.gathering.command.domain.enums.GatheringRole

data class CommandGatheringAuthorizationUpdateDTO(
    val gatheringId: String,
    val memberId: String,
    val gatheringRole: GatheringRole,
    val idempotencyKey: String,
)