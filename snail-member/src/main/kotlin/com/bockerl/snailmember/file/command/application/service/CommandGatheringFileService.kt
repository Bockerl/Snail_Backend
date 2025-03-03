/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */

package com.bockerl.snailmember.file.command.application.service

import com.bockerl.snailmember.file.command.domain.aggregate.entity.File

interface CommandGatheringFileService {
    fun createGatheringFile(
        fileId: Long,
        gatheringId: Long,
        file: File,
    )

    fun deleteGatheringFile(fileIds: List<Long>)
}
