/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */

@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.file.command.application.service

import com.bockerl.snailmember.file.command.application.dto.*
import org.springframework.web.multipart.MultipartFile

interface CommandFileService {
    fun createSingleFile(
        file: MultipartFile,
        commandFileDTO: CommandFileDTO,
    )

    fun createFiles(
        files: List<MultipartFile>,
        commandFileDTO: CommandFileDTO,
    )

    fun createFilesWithGatheringId(
        files: List<MultipartFile>,
        commandFileWithGatheringDTO: CommandFileWithGatheringDTO,
    )

    fun updateProfileImage(
        file: MultipartFile,
        commandFileDTO: CommandFileDTO,
    )

    fun updateFiles(
        commandFileDTO: CommandFileDTO,
        deletedFileIds: List<Long>,
        newFiles: List<MultipartFile>,
    )

    fun updateFilesWithGatheringId(
        commandFileWithGatheringDTO: CommandFileWithGatheringDTO,
        deletedFileIds: List<Long>,
        newFiles: List<MultipartFile>,
    )

    fun deleteFile(commandFileDTO: CommandFileDTO)

    fun createFileEvent(commandFileCreateDTO: CommandFileCreateDTO)

    fun deleteFileEvent(commandFileDeleteDTO: CommandFileDeleteDTO)

    fun createGatheringFileEvent(commandFileWithGatheringCreateDTO: CommandFileWithGatheringCreateDTO)
}