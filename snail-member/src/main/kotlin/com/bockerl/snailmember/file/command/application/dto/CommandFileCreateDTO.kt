package com.bockerl.snailmember.file.command.application.dto

import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType

data class CommandFileCreateDTO(
    val fileName: String,
    val fileUrl: String,
    val fileType: String,
    val fileTargetType: FileTargetType,
    val fileTargetId: String,
    val memberId: String,
)