/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.file.command.domain.service

import com.azure.storage.blob.BlobContainerClient
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.file.command.application.dto.*
import com.bockerl.snailmember.file.command.application.service.CommandFileService
import com.bockerl.snailmember.file.command.application.service.CommandGatheringFileService
import com.bockerl.snailmember.file.command.domain.aggregate.entity.File
import com.bockerl.snailmember.file.command.domain.aggregate.event.FileCreatedEvent
import com.bockerl.snailmember.file.command.domain.aggregate.event.FileDeletedEvent
import com.bockerl.snailmember.file.command.domain.aggregate.event.GatheringFileCreatedEvent
import com.bockerl.snailmember.file.command.domain.repository.CommandFileRepository
import com.bockerl.snailmember.infrastructure.outbox.dto.OutboxDTO
import com.bockerl.snailmember.infrastructure.outbox.enums.EventType
import com.bockerl.snailmember.infrastructure.outbox.service.OutboxService
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class CommandFileServiceImpl(
    private val blobContainerClient: BlobContainerClient,
    private val commandFileRepository: CommandFileRepository,
    private val gatheringFileService: CommandGatheringFileService,
    private val objectMapper: ObjectMapper,
    private val outboxService: OutboxService,
) : CommandFileService {
    private val logger = KotlinLogging.logger {}

    @Transactional
    override fun createSingleFile(
        file: MultipartFile,
        commandFileDTO: CommandFileDTO,
    ): String {
        val fileName = generateUniqueFileName(file.originalFilename)
        val blobClient = blobContainerClient.getBlobClient(fileName)

        try {
            // Blob Storage에 파일 업로드 시도
            blobClient.upload(file.inputStream, file.size, true)
        } catch (ex: Exception) {
            // 업로드 실패 시 로그 기록 후 사용자 정의 예외 전환
            logger.error { "Blob storage 업로드 실패: 파일명 $fileName, $ex" }
            throw CommonException(ErrorCode.BLOB_STORAGE_ERROR)
        }

        val fileUrl = blobClient.blobUrl

        val event =
            FileCreatedEvent(
                fileName = fileName,
                fileType = file.contentType ?: "unknown",
                fileUrl = fileUrl,
                fileTargetType = commandFileDTO.fileTargetType,
                fileTargetId = commandFileDTO.fileTargetId,
                memberId = commandFileDTO.memberId,
            )

        val jsonPayload = objectMapper.writeValueAsString(event)

        logger.info { "idempotencykey: " + commandFileDTO.idempotencyKey }

        // 설명. 파일에서는 aggregateId에 fileName을 넣겠습니다.
        val outbox =
            OutboxDTO(
                aggregateId = fileName,
                eventType = EventType.FILE_CREATED,
                payload = jsonPayload,
                idempotencyKey = commandFileDTO.idempotencyKey,
            )

        outboxService.createOutbox(outbox)
        return fileUrl
    }

    @Transactional
    override fun createFiles(
        files: List<MultipartFile>,
        commandFileDTO: CommandFileDTO,
    ) {
        val outboxEvents = mutableListOf<OutboxDTO>()

        // 설명. 업로드 파일 수 제한 10개 초과 되지 않도록...
        if (files.size > 10) {
            throw CommonException(ErrorCode.TOO_MANY_FILES)
        }

        files.forEach { file ->
            val fileName = generateUniqueFileName(file.originalFilename)
            val blobClient = blobContainerClient.getBlobClient(fileName)
            try {
                blobClient.upload(file.inputStream, file.size, true)
            } catch (ex: Exception) {
                logger.error { "Blob storage 업로드 실패: 파일명 $fileName, $ex" }
                throw CommonException(ErrorCode.BLOB_STORAGE_ERROR)
            }
            val fileUrl = blobClient.blobUrl

            // 각 파일마다 개별 이벤트 생성
            val event =
                FileCreatedEvent(
                    fileName = fileName,
                    fileType = file.contentType ?: "unknown",
                    fileUrl = fileUrl,
                    fileTargetType = commandFileDTO.fileTargetType,
                    fileTargetId = commandFileDTO.fileTargetId,
                    memberId = commandFileDTO.memberId,
                )
            val jsonPayload = objectMapper.writeValueAsString(event)
            // 여기서는 파일 이름을 aggregateId로 활용합니다.
            val outbox =
                OutboxDTO(
                    aggregateId = fileName,
                    eventType = EventType.FILE_CREATED,
                    payload = jsonPayload,
//                    idempotencyKey = commandFileDTO.idempotencyKey,
                )
            outboxEvents.add(outbox)
        }

        outboxService.createOutboxes(outboxEvents)
    }

    @Transactional
    override fun createFilesWithGatheringId(
        files: List<MultipartFile>,
        commandFileWithGatheringDTO: CommandFileWithGatheringDTO,
    ) {
        val outboxEvents = mutableListOf<OutboxDTO>()

        // 설명. targetType이 앨범 도메인 일 시 초과 체크 x
//        if(commandFileWithGatheringRequestVO.fileTargetType != "album") {}

        // 설명. 업로드 파일 수 제한 10개 초과 되지 않도록...
        if (files.size > 10) {
            throw CommonException(ErrorCode.TOO_MANY_FILES)
        }

        files.forEach { file ->
            val fileName = generateUniqueFileName(file.originalFilename)
            val blobClient = blobContainerClient.getBlobClient(fileName)
            try {
                blobClient.upload(file.inputStream, file.size, true)
            } catch (ex: Exception) {
                logger.error { "Blob storage 업로드 실패: 파일명 $fileName, $ex" }
                throw CommonException(ErrorCode.BLOB_STORAGE_ERROR)
            }
            val fileUrl = blobClient.blobUrl

            // 각 파일마다 개별 이벤트 생성
            val event =
                GatheringFileCreatedEvent(
                    fileName = fileName,
                    fileType = file.contentType ?: "unknown",
                    fileUrl = fileUrl,
                    fileTargetType = commandFileWithGatheringDTO.fileTargetType,
                    fileTargetId = commandFileWithGatheringDTO.fileTargetId,
                    memberId = commandFileWithGatheringDTO.memberId,
                    gatheringId = commandFileWithGatheringDTO.gatheringId,
                )
            val jsonPayload = objectMapper.writeValueAsString(event)
            // 여기서는 파일 이름을 aggregateId로 활용합니다.
            val outbox =
                OutboxDTO(
                    aggregateId = fileName,
                    eventType = EventType.FILE_CREATED,
                    payload = jsonPayload,
//                    idempotencyKey = commandFileWithGatheringDTO.idempotencyKey,
                )
            outboxEvents.add(outbox)
        }

        // 파일 정보 저장
        outboxService.createOutboxes(outboxEvents)
    }

    // 설명. 삭제 후 재생성
    @Transactional
    override fun updateProfileImage(
        file: MultipartFile,
        commandFileDTO: CommandFileDTO,
    ): String {
        if (!file.contentType?.startsWith("image/")!!) {
            throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT)
        }
        val existingFile =
            commandFileRepository.findByFileTargetTypeAndFileTargetId(
                commandFileDTO.fileTargetType,
                extractDigits(commandFileDTO.fileTargetId),
            )

        val blobClient = blobContainerClient.getBlobClient(existingFile[0].fileName)

        try {
            blobClient.delete()
        } catch (ex: Exception) {
            logger.error { "Blob storage 삭제 실패: 파일명 ${existingFile[0].fileName}, $ex" }
            throw CommonException(ErrorCode.BLOB_STORAGE_ERROR)
        }

        val deleteEvent =
            FileDeletedEvent(
                fileTargetId = commandFileDTO.fileTargetId,
                fileTargetType = commandFileDTO.fileTargetType,
            )

        val deleteJsonPayload = objectMapper.writeValueAsString(deleteEvent)

        val deleteOutBox =
            OutboxDTO(
                aggregateId = existingFile[0].fileName,
                eventType = EventType.FILE_DELETED,
                payload = deleteJsonPayload,
//                idempotencyKey = commandFileDTO.idempotencyKey,
            )

        outboxService.createOutbox(deleteOutBox)

        val fileName = generateUniqueFileName(file.originalFilename)
        blobContainerClient.getBlobClient(fileName)
        try {
            blobClient.upload(file.inputStream, file.size, true)
        } catch (ex: Exception) {
            logger.error { "Blob storage 업로드 실패: 파일명 $fileName, $ex" }
            throw CommonException(ErrorCode.BLOB_STORAGE_ERROR)
        }
        val fileUrl = blobClient.blobUrl

        val event =
            FileCreatedEvent(
                fileName = fileName,
                fileType = file.contentType ?: "unknown",
                fileUrl = fileUrl,
                fileTargetType = commandFileDTO.fileTargetType,
                fileTargetId = commandFileDTO.fileTargetId,
                memberId = commandFileDTO.memberId,
            )
        val createJsonPayload = objectMapper.writeValueAsString(event)
        // 여기서는 파일 이름을 aggregateId로 활용합니다.
        val createOutbox =
            OutboxDTO(
                aggregateId = fileName,
                eventType = EventType.FILE_CREATED,
                payload = createJsonPayload,
//                idempotencyKey = commandFileDTO.idempotencyKey,
            )

        outboxService.createOutbox(createOutbox)
        return fileUrl
    }

    // 설명. 삭제할 파일 삭제 후 새로운 파일 추가
    @Transactional
    override fun updateFiles(
        commandFileDTO: CommandFileDTO,
        deletedFileIds: List<Long>,
        newFiles: List<MultipartFile>,
    ) {
        val outboxEvents = mutableListOf<OutboxDTO>()
        val existingFiles =
            commandFileRepository.findByFileTargetTypeAndFileTargetId(
                commandFileDTO.fileTargetType,
                extractDigits(commandFileDTO.fileTargetId),
            )

        // 기존 파일 중 유지할 파일 리스트 (삭제되지 않은 파일들)
        val remainingFiles = existingFiles.filterNot { it.fileId in deletedFileIds }

        // 파일 개수 제한
        if (newFiles.size + remainingFiles.size > 10) {
            throw CommonException(ErrorCode.TOO_MANY_FILES)
        }

        // 1. 삭제할 파일 처리: Blob Storage에서 삭제 + 삭제 이벤트 발행
        if (deletedFileIds.isNotEmpty()) {
            val filesToDelete = existingFiles.filter { it.fileId in deletedFileIds }
            filesToDelete.forEach { file ->
                val blobClient = blobContainerClient.getBlobClient(file.fileName)
                try {
                    blobClient.delete()
                } catch (ex: Exception) {
                    logger.error { "Blob storage 삭제 실패: 파일명 ${file.fileName}, $ex" }
                    throw CommonException(ErrorCode.BLOB_STORAGE_ERROR)
                }

                // 삭제 이벤트 생성
                val deleteEvent =
                    FileDeletedEvent(
                        fileTargetId = commandFileDTO.fileTargetId,
                        fileTargetType = commandFileDTO.fileTargetType,
                    )
                val deleteJsonPayload = objectMapper.writeValueAsString(deleteEvent)
                val deleteOutbox =
                    OutboxDTO(
                        aggregateId = file.fileName, // fileName을 aggregateId로 사용
                        eventType = EventType.FILE_DELETED,
                        payload = deleteJsonPayload,
                        idempotencyKey = commandFileDTO.idempotencyKey,
                    )
                outboxEvents.add(deleteOutbox)
            }
        }

        // 3. 새 파일 업로드 및 생성 이벤트 발행
        val newFileEntities = mutableListOf<File>()
        newFiles.forEach { file ->
            val fileName = generateUniqueFileName(file.originalFilename)
            val blobClient = blobContainerClient.getBlobClient(fileName)
            try {
                blobClient.upload(file.inputStream, file.size, true)
            } catch (ex: Exception) {
                logger.error { "Blob storage 업로드 실패: 파일명 $fileName, $ex" }
                throw CommonException(ErrorCode.BLOB_STORAGE_ERROR)
            }
            val fileUrl = blobClient.blobUrl

            // 새 파일 엔티티 생성
            val fileEntity =
                File(
                    fileName = fileName,
                    fileType = file.contentType ?: "unknown",
                    fileUrl = fileUrl,
                    fileTargetType = commandFileDTO.fileTargetType,
                    fileTargetId = extractDigits(commandFileDTO.fileTargetId),
                    memberId = extractDigits(commandFileDTO.memberId),
                )
            newFileEntities.add(fileEntity)

            // 생성 이벤트 생성
            val createEvent =
                FileCreatedEvent(
                    fileName = fileName,
                    fileType = file.contentType ?: "unknown",
                    fileUrl = fileUrl,
                    fileTargetType = commandFileDTO.fileTargetType,
                    fileTargetId = commandFileDTO.fileTargetId,
                    memberId = commandFileDTO.memberId,
                )
            val createJsonPayload = objectMapper.writeValueAsString(createEvent)
            val createOutbox =
                OutboxDTO(
                    aggregateId = fileName, // fileName을 aggregateId로 사용
                    eventType = EventType.FILE_CREATED,
                    payload = createJsonPayload,
                    idempotencyKey = commandFileDTO.idempotencyKey,
                )
            outboxEvents.add(createOutbox)
        }

        // 설명. outbox 테이블에 모든 이벤트 보내기
        outboxService.createOutboxes(outboxEvents)
    }

    // 설명. 모임 삭제할 파일 삭제 후 새로운 파일 추가
    @Transactional
    override fun updateFilesWithGatheringId(
        commandFileWithGatheringDTO: CommandFileWithGatheringDTO,
        deletedFileIds: List<Long>,
        newFiles: List<MultipartFile>,
    ) {
        val outboxEvents = mutableListOf<OutboxDTO>()

        val existingFiles =
            commandFileRepository.findByFileTargetTypeAndFileTargetId(
                commandFileWithGatheringDTO.fileTargetType,
                extractDigits(commandFileWithGatheringDTO.fileTargetId),
            )

        val remainingFiles = existingFiles.filterNot { it.fileId in deletedFileIds }

        if (newFiles.size + remainingFiles.size > 10) {
            throw CommonException(ErrorCode.TOO_MANY_FILES)
        }

        // 1. 삭제할 파일 처리: Blob Storage에서 삭제 + 삭제 이벤트 발행
        if (deletedFileIds.isNotEmpty()) {
            val filesToDelete = existingFiles.filter { it.fileId in deletedFileIds }
            filesToDelete.forEach { file ->
                val blobClient = blobContainerClient.getBlobClient(file.fileName)
                try {
                    blobClient.delete()
                } catch (ex: Exception) {
                    logger.error { "Blob storage 삭제 실패: 파일명 ${file.fileName}, $ex" }
                    throw CommonException(ErrorCode.BLOB_STORAGE_ERROR)
                }

                // 삭제 이벤트 생성
                val deleteEvent =
                    FileDeletedEvent(
                        fileTargetId = commandFileWithGatheringDTO.fileTargetId,
                        fileTargetType = commandFileWithGatheringDTO.fileTargetType,
                    )
                val deleteJsonPayload = objectMapper.writeValueAsString(deleteEvent)
                val deleteOutbox =
                    OutboxDTO(
                        aggregateId = file.fileName, // fileName을 aggregateId로 사용
                        eventType = EventType.FILE_DELETED,
                        payload = deleteJsonPayload,
                        idempotencyKey = commandFileWithGatheringDTO.idempotencyKey,
                    )
                outboxEvents.add(deleteOutbox)
            }
        }

        // 3. 새 파일 업로드 및 생성 이벤트 발행
        val newFileEntities = mutableListOf<File>()
        newFiles.forEach { file ->
            val fileName = generateUniqueFileName(file.originalFilename)
            val blobClient = blobContainerClient.getBlobClient(fileName)
            try {
                blobClient.upload(file.inputStream, file.size, true)
            } catch (ex: Exception) {
                logger.error { "Blob storage 업로드 실패: 파일명 $fileName, $ex" }
                throw CommonException(ErrorCode.BLOB_STORAGE_ERROR)
            }
            val fileUrl = blobClient.blobUrl

            // 새 파일 엔티티 생성
            val fileEntity =
                File(
                    fileName = fileName,
                    fileType = file.contentType ?: "unknown",
                    fileUrl = fileUrl,
                    fileTargetType = commandFileWithGatheringDTO.fileTargetType,
                    fileTargetId = extractDigits(commandFileWithGatheringDTO.fileTargetId),
                    memberId = extractDigits(commandFileWithGatheringDTO.memberId),
                )
            newFileEntities.add(fileEntity)

            // 생성 이벤트 생성
            val createEvent =
                GatheringFileCreatedEvent(
                    fileName = fileName,
                    fileType = file.contentType ?: "unknown",
                    fileUrl = fileUrl,
                    fileTargetType = commandFileWithGatheringDTO.fileTargetType,
                    fileTargetId = commandFileWithGatheringDTO.fileTargetId,
                    memberId = commandFileWithGatheringDTO.memberId,
                    gatheringId = commandFileWithGatheringDTO.gatheringId,
                )
            val createJsonPayload = objectMapper.writeValueAsString(createEvent)
            val createOutbox =
                OutboxDTO(
                    aggregateId = fileName, // fileName을 aggregateId로 사용
                    eventType = EventType.FILE_CREATED,
                    payload = createJsonPayload,
                    idempotencyKey = commandFileWithGatheringDTO.idempotencyKey,
                )
            outboxEvents.add(createOutbox)
        }
        // 설명. outbox 테이블에 모든 이벤트 보내기
        outboxService.createOutboxes(outboxEvents)
    }

    /* 설명.
     *  fileName에 해당하는 Blob을 삭제함.
     * */
    @Transactional
    override fun deleteFile(commandFileDTO: CommandFileDTO) {
        val outboxEvents = mutableListOf<OutboxDTO>()

        val files =
            commandFileRepository.findByFileTargetTypeAndFileTargetId(
                commandFileDTO.fileTargetType,
                extractDigits(commandFileDTO.fileTargetId),
            )

        if (files.isNotEmpty()) {
//            commandFileRepository.updateActiveAndFileUrlByFileTargetIdAndFileTargetType(
//                extractDigits(commandFileDTO.fileTargetId),
//                commandFileDTO.fileTargetType,
//            )
            files.forEach { file ->
                val blobClient = blobContainerClient.getBlobClient(file.fileName)
                try {
                    blobClient.delete()
                } catch (ex: Exception) {
                    logger.error { "Blob storage 삭제 실패: 파일명 ${file.fileName}, $ex" }
                    throw CommonException(ErrorCode.BLOB_STORAGE_ERROR)
                }

                // 삭제 이벤트 생성
                val deleteEvent =
                    FileDeletedEvent(
                        fileTargetType = commandFileDTO.fileTargetType,
                        fileTargetId = commandFileDTO.fileTargetId,
                    )
                val deleteJsonPayload = objectMapper.writeValueAsString(deleteEvent)
                val deleteOutbox =
                    OutboxDTO(
                        aggregateId = file.fileName, // fileName을 aggregateId로 사용
                        eventType = EventType.FILE_DELETED,
                        payload = deleteJsonPayload,
//                        idempotencyKey = commandFileDTO.idempotencyKey,
                    )
                outboxEvents.add(deleteOutbox)
            }
        }
        outboxService.createOutboxes(outboxEvents)
    }

    @Transactional
    override fun createFileEvent(commandFileCreateDTO: CommandFileCreateDTO) {
        val fileEntity =
            File(
                fileName = commandFileCreateDTO.fileName,
                fileUrl = commandFileCreateDTO.fileUrl,
                fileType = commandFileCreateDTO.fileType,
                fileTargetType = commandFileCreateDTO.fileTargetType,
                fileTargetId = extractDigits(commandFileCreateDTO.fileTargetId),
                memberId = extractDigits(commandFileCreateDTO.memberId),
            )

        commandFileRepository.save(fileEntity)
    }

    @Transactional
    override fun createGatheringFileEvent(commandFileWithGatheringCreateDTO: CommandFileWithGatheringCreateDTO) {
        val fileEntity =
            File(
                fileName = commandFileWithGatheringCreateDTO.fileName,
                fileUrl = commandFileWithGatheringCreateDTO.fileUrl,
                fileType = commandFileWithGatheringCreateDTO.fileType,
                fileTargetType = commandFileWithGatheringCreateDTO.fileTargetType,
                fileTargetId = extractDigits(commandFileWithGatheringCreateDTO.fileTargetId),
                memberId = extractDigits(commandFileWithGatheringCreateDTO.memberId),
            )

        val savedFile = commandFileRepository.save(fileEntity)

        gatheringFileService.createGatheringFile(
            savedFile.fileId!!,
            extractDigits(commandFileWithGatheringCreateDTO.gatheringId),
            savedFile,
        )
    }

    @Transactional
    override fun deleteFileEvent(commandFileDeleteDTO: CommandFileDeleteDTO) {
        commandFileRepository.updateActiveAndFileUrlByFileTargetIdAndFileTargetType(
            extractDigits(commandFileDeleteDTO.fileTargetId),
            commandFileDeleteDTO.fileTargetType,
        )
    }

    // 설명. uuid 생성으로 파일 이름 중복 방지
    private fun generateUniqueFileName(originalFileName: String?): String {
        val uuid = UUID.randomUUID().toString()
        // 설명. 확장자 파싱
        val extension = originalFileName?.substringAfterLast(".", "") ?: ""
        return if (extension.isNotEmpty()) "$uuid.$extension" else uuid
    }

    private fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()
}