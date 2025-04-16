package com.bockerl.snailmember.gathering.command.application.dto

data class CommandGatheringMemberCreateDTO(
    val gatheringId: String,
    val memberId: String,
    val idempotencyKey: String,
)