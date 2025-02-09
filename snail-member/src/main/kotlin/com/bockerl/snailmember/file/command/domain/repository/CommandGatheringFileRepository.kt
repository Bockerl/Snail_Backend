package com.bockerl.snailmember.file.command.domain.repository

import com.bockerl.snailmember.file.command.domain.aggregate.entity.GatheringFile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CommandGatheringFileRepository : JpaRepository<GatheringFile, Long> {
}