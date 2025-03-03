/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.file.command.domain.repository

import com.bockerl.snailmember.file.command.domain.aggregate.entity.File
import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CommandFileRepository : JpaRepository<File, Long> {
    @Query("SELECT f FROM File f WHERE f.active = true and f.fileTargetType = :fileTargetType and f.fileTargetId = :fileTargetId")
    fun findByFileTargetTypeAndFileTargetId(
        fileTargetType: FileTargetType?,
        fileTargetId: Long?,
    ): List<File>

    @Modifying
    @Query(
        "UPDATE File f SET f.active = false, f.fileUrl = '' WHERE f.fileTargetType = :fileTargetType and f.fileTargetId = :fileTargetId and f.active = true",
    )
    fun updateActiveAndFileUrlByFileTargetIdAndFileTargetType(
        @Param("fileTargetId") fileTargetId: Long?,
        @Param("fileTargetType") fileTargetType: FileTargetType?,
    )

    @Modifying
    @Query("UPDATE File f SET f.active = false, f.fileUrl = '' WHERE f.fileId = :fileId and f.active = true")
    fun updateActiveAndFileUrlByFileId(fileId: Long?)

    @Modifying
    @Query("UPDATE File f SET f.active = false, f.fileUrl = '' WHERE f.active= true and f.fileId in :deletedFileIds")
    fun updateActiveAndFileUrlByDeletedFileIds(deletedFileIds: List<Long>?)
}