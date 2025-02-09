package com.bockerl.snailmember.blob.command.domain.repository

import com.bockerl.snailmember.blob.command.domain.aggregate.entity.File
import com.bockerl.snailmember.blob.command.domain.aggregate.enums.FileTargetType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CommandFileRepository : JpaRepository<File, Long> {
    fun findByFileTargetTypeAndFileTargetId(fileTargetType: FileTargetType?, fileTargetId: Long?): List<File>

    @Query("SELECT COALESCE(MAX(f.fileOrder), 0) FROM File f WHERE f.fileTargetType = :fileTargetType AND f.fileTargetId = :fileTargetId")
    fun findMaxFileOrderByFileTargetTypeAndFileTargetId(fileTargetType: FileTargetType?, fileTargetId: Long): Int
//    fun findMaxFileOrderByFileTargetTypeAndFileTargetId(fileTargetType: FileTargetType?, fileTargetId: Long?): Int
}