package com.bockerl.snailmember.file.command.domain.aggregate.event

import com.bockerl.snailmember.common.event.BaseFileCreatedEvent
import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType

data class FileCreatedEvent(
    override val fileName: String,
    override val fileUrl: String,
    override val fileType: String,
    override val fileTargetType: FileTargetType,
    override val fileTargetId: String,
    override val memberId: String,
) : BaseFileCreatedEvent