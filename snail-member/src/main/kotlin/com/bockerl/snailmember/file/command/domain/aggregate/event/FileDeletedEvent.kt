package com.bockerl.snailmember.file.command.domain.aggregate.event

data class FileDeletedEvent(
    val fileId: String?,
)