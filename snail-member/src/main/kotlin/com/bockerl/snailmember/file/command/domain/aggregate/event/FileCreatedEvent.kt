package com.bockerl.snailmember.file.command.domain.aggregate.event

import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType

data class FileCreatedEvent(
    val fileName: String,
    val fileUrl: String,
    val fileType: String,
    val fileTargetType: FileTargetType,
    val fileTargetId: Long,
    val memberId: Long,
)