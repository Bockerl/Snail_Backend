package com.bockerl.snailmember.file.command.domain.aggregate.event

import com.bockerl.snailmember.common.event.BaseFileEvent
import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType

data class FileDeletedEvent(
    override val fileTargetType: FileTargetType,
    override val fileTargetId: String,
) : BaseFileEvent