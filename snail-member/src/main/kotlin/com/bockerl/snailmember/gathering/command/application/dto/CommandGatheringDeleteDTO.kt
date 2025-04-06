package com.bockerl.snailmember.gathering.command.application.dto

data class CommandGatheringDeleteDTO(
    var gatheringId: String,
    val memberId: String,
    val idempotencyKey: String,
)