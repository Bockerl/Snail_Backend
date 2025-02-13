/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.file.command.domain.service

import com.azure.storage.blob.BlobContainerClient
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.file.command.application.service.CommandFileService
import com.bockerl.snailmember.file.command.application.service.CommandGatheringFileService
import com.bockerl.snailmember.file.command.domain.aggregate.entity.File
import com.bockerl.snailmember.file.command.domain.aggregate.vo.CommandFileRequestVO
import com.bockerl.snailmember.file.command.domain.aggregate.vo.CommandFileWithGatheringRequestVO
import com.bockerl.snailmember.file.command.domain.repository.CommandFileRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream
import java.util.*

@Service
class CommandFileServiceImpl(
    private val blobContainerClient: BlobContainerClient,
    private val commandFileRepository: CommandFileRepository,
    private val gatheringFileService: CommandGatheringFileService,
) : CommandFileService {
    @Transactional
    override fun uploadProfileImage(
        file: MultipartFile,
        commandFileRequestVO: CommandFileRequestVO,
    ) {
        val fileName = generateUniqueFileName(file.originalFilename)
        val blobClient = blobContainerClient.getBlobClient(fileName)
        blobClient.upload(file.inputStream, file.size, true)
        val fileUrl = blobClient.blobUrl

        val fileEntity =
            File(
                fileName = fileName,
                fileType = file.contentType ?: "unknown",
                fileUrl = fileUrl,
                fileTargetType = commandFileRequestVO.fileTargetType,
                fileTargetId = commandFileRequestVO.fileTargetId,
                memberId = commandFileRequestVO.memberId,
            )

        commandFileRepository.save(fileEntity)
    }

    @Transactional
    override fun uploadFiles(
        files: List<MultipartFile>,
        commandFileRequestVO: CommandFileRequestVO,
    ) {
        val fileEntities = mutableListOf<File>()

        // 설명. 업로드 파일 수 제한 10개 초과 되지 않도록...
        if (files.size > 10) {
            throw CommonException(ErrorCode.TOO_MANY_FILES)
        }

        files.forEach { file ->
            val fileName = generateUniqueFileName(file.originalFilename)
            val blobClient = blobContainerClient.getBlobClient(fileName)
            blobClient.upload(file.inputStream, file.size, true)
            val fileUrl = blobClient.blobUrl

            val fileEntity =
                File(
                    fileName = fileName,
                    fileType = file.contentType ?: "unknown",
                    fileUrl = fileUrl,
                    fileTargetType = commandFileRequestVO.fileTargetType,
                    fileTargetId = commandFileRequestVO.fileTargetId,
                    memberId = commandFileRequestVO.memberId,
                )
            fileEntities.add(fileEntity)
        }

        commandFileRepository.saveAll(fileEntities)
    }

    @Transactional
    override fun uploadFilesWithGatheringId(
        files: List<MultipartFile>,
        commandFileWithGatheringRequestVO: CommandFileWithGatheringRequestVO,
    ) {
        val fileEntities = mutableListOf<File>()

        // 설명. targetType이 앨범 도메인 일 시 초과 체크 x
//        if(commandFileWithGatheringRequestVO.fileTargetType != "album") {}

        // 설명. 업로드 파일 수 제한 10개 초과 되지 않도록...
        if (files.size > 10) {
            throw CommonException(ErrorCode.TOO_MANY_FILES)
        }
        files.forEach { file ->
            val fileName = generateUniqueFileName(file.originalFilename)
            val blobClient = blobContainerClient.getBlobClient(fileName)
            blobClient.upload(file.inputStream, file.size, true)
            val fileUrl = blobClient.blobUrl

            val fileEntity =
                File(
                    fileName = fileName,
                    fileType = file.contentType ?: "unknown",
                    fileUrl = fileUrl,
                    fileTargetType = commandFileWithGatheringRequestVO.fileTargetType,
                    fileTargetId = commandFileWithGatheringRequestVO.fileTargetId,
                    memberId = commandFileWithGatheringRequestVO.memberId,
                )
            fileEntities.add(fileEntity)
        }

        // 파일 정보 저장
        val savedFiles = commandFileRepository.saveAll(fileEntities)

        // fileId와 gatheringId를 이용해 GatheringFile 저장
        savedFiles.forEach { file ->
            gatheringFileService.createGatheringFile(file.fileId!!, commandFileWithGatheringRequestVO.gatheringId, file)
        }
    }

    // 설명. 삭제 후 재생성
    @Transactional
    override fun updateProfileImage(
        file: MultipartFile,
        commandFileRequestVO: CommandFileRequestVO,
    ) {
        if (!file.contentType?.startsWith("image/")!!) {
            throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT)
        }
        val existingFile =
            commandFileRepository.findByFileTargetTypeAndFileTargetId(
                commandFileRequestVO.fileTargetType,
                commandFileRequestVO.fileTargetId,
            )

        val blobClient = blobContainerClient.getBlobClient(existingFile[0].fileName)
        blobClient.delete()

        commandFileRepository.delete(existingFile[0])

        val fileName = generateUniqueFileName(file.originalFilename)
        blobContainerClient.getBlobClient(fileName)
        blobClient.upload(file.inputStream, file.size, true)
        val fileUrl = blobClient.blobUrl

        val fileEntity =
            File(
                fileName = fileName,
                fileType = file.contentType ?: "unknown",
                fileUrl = fileUrl,
                fileTargetType = commandFileRequestVO.fileTargetType,
                fileTargetId = commandFileRequestVO.fileTargetId,
                memberId = commandFileRequestVO.memberId,
            )

        commandFileRepository.save(fileEntity)
    }

    // 설명. 삭제할 파일 삭제 후 새로운 파일 추가
    @Transactional
    override fun updateFiles(
        commandFileRequestVO: CommandFileRequestVO,
        deletedFileIds: List<Long>,
        newFiles: List<MultipartFile>,
    ) {
        val existingFiles =
            commandFileRepository.findByFileTargetTypeAndFileTargetId(
                commandFileRequestVO.fileTargetType,
                commandFileRequestVO.fileTargetId,
            )

        // 삭제할 파일만 삭제
        if (deletedFileIds.isNotEmpty()) {
            val filesToDelete = existingFiles.filter { it.fileId in deletedFileIds }
            filesToDelete.forEach { file ->
                val blobClient = blobContainerClient.getBlobClient(file.fileName)
                blobClient.delete()
            }
            commandFileRepository.deleteAll(filesToDelete)
        }

        // 기존 파일 중 유지할 파일 리스트 (삭제되지 않은 파일들)
        val remainingFiles = existingFiles.filterNot { it.fileId in deletedFileIds }

        // 새 파일 추가
        val newFileEntities =
            newFiles.mapIndexed { index, file ->
                val fileName = generateUniqueFileName(file.originalFilename)
                val blobClient = blobContainerClient.getBlobClient(fileName)
                blobClient.upload(file.inputStream, file.size, true)

                File(
                    fileName = fileName,
                    fileType = file.contentType ?: "unknown",
                    fileUrl = blobClient.blobUrl,
                    fileTargetType = commandFileRequestVO.fileTargetType,
                    fileTargetId = commandFileRequestVO.fileTargetId,
                    memberId = commandFileRequestVO.memberId,
                )
            }

        // 파일 개수 초과 체크
        if (remainingFiles.size + newFileEntities.size > 10) {
            throw CommonException(ErrorCode.TOO_MANY_FILES)
        }

        // 새 파일 저장
        commandFileRepository.saveAll(newFileEntities)
    }

    // 설명. 모임 삭제할 파일 삭제 후 새로운 파일 추가
    @Transactional
    override fun updateFilesWithGatheringId(
        commandFileWithGatheringRequestVO: CommandFileWithGatheringRequestVO,
        deletedFileIds: List<Long>,
        newFiles: List<MultipartFile>,
    ) {
        val existingFiles =
            commandFileRepository.findByFileTargetTypeAndFileTargetId(
                commandFileWithGatheringRequestVO.fileTargetType,
                commandFileWithGatheringRequestVO.fileTargetId,
            )

        // 삭제할 파일만 삭제
        if (deletedFileIds.isNotEmpty()) {
            val filesToDelete = existingFiles.filter { it.fileId in deletedFileIds }
            filesToDelete.forEach { file ->
                val blobClient = blobContainerClient.getBlobClient(file.fileName)
                blobClient.delete()
            }
            commandFileRepository.deleteAll(filesToDelete)
            // 설명. cascade 설정으로 해당 gatheringFile도 같이 삭제
        }

        // 기존 파일 중 유지할 파일 리스트 (삭제되지 않은 파일들)
        val remainingFiles = existingFiles.filterNot { it.fileId in deletedFileIds }

        // 새 파일 추가
        val newFileEntities =
            newFiles.mapIndexed { index, file ->
                val fileName = generateUniqueFileName(file.originalFilename)
                val blobClient = blobContainerClient.getBlobClient(fileName)
                blobClient.upload(file.inputStream, file.size, true)

                File(
                    fileName = fileName,
                    fileType = file.contentType ?: "unknown",
                    fileUrl = blobClient.blobUrl,
                    fileTargetType = commandFileWithGatheringRequestVO.fileTargetType,
                    fileTargetId = commandFileWithGatheringRequestVO.fileTargetId,
                    memberId = commandFileWithGatheringRequestVO.memberId,
                )
            }

        // 파일 개수 초과 체크
        if (remainingFiles.size + newFileEntities.size > 10) {
            throw CommonException(ErrorCode.TOO_MANY_FILES)
        }

        // 파일 정보 저장
        val savedFiles = commandFileRepository.saveAll(newFileEntities)

        // fileId와 gatheringId를 이용해 GatheringFile 저장
        savedFiles.forEach { file ->
            gatheringFileService.createGatheringFile(file.fileId!!, commandFileWithGatheringRequestVO.gatheringId, file)
        }
    }

    /* 설명.
     *  fileName에 해당하는 Blob을 삭제함.
     * */
    @Transactional
    override fun deleteFile(commandFileRequestVO: CommandFileRequestVO) {
        val files =
            commandFileRepository.findByFileTargetTypeAndFileTargetId(
                commandFileRequestVO.fileTargetType,
                commandFileRequestVO.fileTargetId,
            )

        for (file in files) {
            val blobClient = blobContainerClient.getBlobClient(file.fileName)
            blobClient.delete()
        }

        commandFileRepository.deleteAll(files)
    }

    /* 설명.
     *  Filename을 받아 Blob에서 파일을 다운로드함.
     *  ByteArrayOutputStream을 사용해서 바이너리 데이터를 읽어옴
     * */
    @Transactional
    override fun downloadFile(fileName: String): ByteArray {
        val blobClient = blobContainerClient.getBlobClient(fileName)
        val outputStream = ByteArrayOutputStream()
        blobClient.download(outputStream)
        return outputStream.toByteArray()
    }

    // 설명. uuid 생성으로 파일 이름 중복 방지
    private fun generateUniqueFileName(originalFileName: String?): String {
        val uuid = UUID.randomUUID().toString()
        // 설명. 확장자 파싱
        val extension = originalFileName?.substringAfterLast(".", "") ?: ""
        return if (extension.isNotEmpty()) "$uuid.$extension" else uuid
    }
}