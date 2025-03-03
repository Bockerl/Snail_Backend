package com.bockerl.snailmember.file.command.application.dto

import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType

data class CommandFileDeleteDTO(
    val fileTargetType: FileTargetType,
    val fileTargetId: String,
)