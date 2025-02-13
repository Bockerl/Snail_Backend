package com.bockerl.snailmember.file.command.domain.repository

import com.bockerl.snailmember.file.command.domain.aggregate.entity.File
import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CommandFileRepository : JpaRepository<File, Long> {
    fun findByFileTargetTypeAndFileTargetId(fileTargetType: FileTargetType?, fileTargetId: Long?): List<File>
}
