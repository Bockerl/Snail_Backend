package com.bockerl.snailmember.file.command.domain.service

import com.bockerl.snailmember.file.command.application.service.CommandGatheringFileService
import com.bockerl.snailmember.file.command.domain.aggregate.entity.File
import com.bockerl.snailmember.file.command.domain.aggregate.entity.GatheringFile
import com.bockerl.snailmember.file.command.domain.repository.CommandGatheringFileRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class CommandGatheringFileServiceImpl(private val commandGatheringFileRepository: CommandGatheringFileRepository) :
    CommandGatheringFileService {

    @Transactional
    override fun createGatheringFile(fileId: Long, gatheringId: Long, file: File) {
        val gatheringFile = GatheringFile(
            fileId = fileId,
            gatheringId = gatheringId,
            file = file,
        )
        commandGatheringFileRepository.save(gatheringFile)
    }

    override fun deleteGatheringFile(fileIds: List<Long>) {
        commandGatheringFileRepository.deleteAllById(fileIds)
    }
}
