/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */

package com.bockerl.snailmember.file.command.application.service

import com.bockerl.snailmember.file.command.domain.aggregate.vo.CommandFileRequestVO
import com.bockerl.snailmember.file.command.domain.aggregate.vo.CommandFileWithGatheringRequestVO
import org.springframework.web.multipart.MultipartFile

interface CommandFileService {
    fun uploadSingleFile(
        file: MultipartFile,
        commandFileRequestVO: CommandFileRequestVO,
    )

    fun uploadFiles(
        file: List<MultipartFile>,
        commandFileRequestVO: CommandFileRequestVO,
    )

    fun uploadFilesWithGatheringId(
        files: List<MultipartFile>,
        commandFileWithGatheringRequestVO: CommandFileWithGatheringRequestVO,
    )

    fun downloadFile(fileName: String): ByteArray

    fun updateProfileImage(
        file: MultipartFile,
        commandFileRequestVO: CommandFileRequestVO,
    )

    fun updateFiles(
        commandFileRequestVO: CommandFileRequestVO,
        deletedFileIds: List<Long>,
        newFiles: List<MultipartFile>,
    )

    fun updateFilesWithGatheringId(
        commandFileWithGatheringRequestVO: CommandFileWithGatheringRequestVO,
        deletedFileIds: List<Long>,
        newFiles: List<MultipartFile>,
    )

    fun deleteFile(commandFileRequestVO: CommandFileRequestVO)
}