package com.bockerl.snailmember.infrastructure.event.handler

import com.bockerl.snailmember.common.event.BaseFileCreatedEvent
import com.bockerl.snailmember.file.command.application.dto.CommandFileCreateDTO
import com.bockerl.snailmember.file.command.application.dto.CommandFileDeleteDTO
import com.bockerl.snailmember.file.command.application.dto.CommandFileWithGatheringCreateDTO
import com.bockerl.snailmember.file.command.application.service.CommandFileService
import com.bockerl.snailmember.file.command.domain.aggregate.event.FileCreatedEvent
import com.bockerl.snailmember.file.command.domain.aggregate.event.FileDeletedEvent
import com.bockerl.snailmember.file.command.domain.aggregate.event.GatheringFileCreatedEvent
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class FileEventHandler(
    private val commandFileService: CommandFileService,
) {
    @Transactional
    fun handleCreate(event: BaseFileCreatedEvent) {
        when (event) {
            is FileCreatedEvent -> {
                val fileDTO =
                    CommandFileCreateDTO(
                        fileName = event.fileName,
                        fileUrl = event.fileUrl,
                        fileType = event.fileType,
                        fileTargetType = event.fileTargetType,
                        fileTargetId = event.fileTargetId,
                        memberId = event.memberId,
                    )

                commandFileService.createFileEvent(fileDTO)
            }

            is GatheringFileCreatedEvent -> {
                val fileDTO =
                    CommandFileWithGatheringCreateDTO(
                        fileName = event.fileName,
                        fileUrl = event.fileUrl,
                        fileType = event.fileType,
                        fileTargetType = event.fileTargetType,
                        fileTargetId = event.fileTargetId,
                        memberId = event.memberId,
                        gatheringId = event.gatheringId,
                    )

                commandFileService.createGatheringFileEvent(fileDTO)
            }
        }
    }

    @Transactional
    fun handleDelete(event: FileDeletedEvent) {
        val fileDTO =
            CommandFileDeleteDTO(
                fileTargetType = event.fileTargetType,
                fileTargetId = event.fileTargetId,
            )

        commandFileService.deleteFileEvent(fileDTO)
    }
}