package com.bockerl.snailmember.file.query.dto

import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType

data class QueryFileRequestDTO(
    val fileTargetType: FileTargetType? = null,
    val fileTargetId: Long,
)