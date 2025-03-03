package com.bockerl.snailmember.file.command.domain.aggregate.event

import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType

data class FileDeletedEvent(
    val fileTargetType: FileTargetType,
    val fileTargetId: String,
)