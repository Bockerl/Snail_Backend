/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */

@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.file.command.application.controller

import com.bockerl.snailmember.common.ResponseDTO
import com.bockerl.snailmember.file.command.application.service.CommandFileService
import com.bockerl.snailmember.file.command.domain.aggregate.vo.CommandFileRequestVO
import com.bockerl.snailmember.file.command.domain.aggregate.vo.CommandFileWithGatheringRequestVO
import com.bockerl.snailmember.infrastructure.config.OpenApiBody
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Encoding
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/file")
class CommandFileController(
    private val commandFileService: CommandFileService,
) {
    @Operation(
        summary = "단일 파일 등록",
        description = "파일 (1장)을 등록합니다. (회원, 모임, gif)",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "단일 파일 등록 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @OpenApiBody(
        description = "단일 파일을 등록합니다.",
        content = [
            Content(
                encoding = [Encoding(name = "commandFileRequestVO", contentType = MediaType.APPLICATION_JSON_VALUE)],
            ),
        ],
        required = true,
    )
    @PostMapping("/single", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun postSingleFile(
        @RequestPart("file") file: MultipartFile,
        @RequestPart("commandFileRequestVO") commandFileRequestVO: CommandFileRequestVO,
    ): ResponseDTO<Void> {
        commandFileService.uploadSingleFile(file, commandFileRequestVO)

        return ResponseDTO.ok(null)
    }

    @Operation(
        summary = "파일(다중) 등록",
        description = "파일(다중)을 등록합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "파일(다중) 등록 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @OpenApiBody(
        description = "타겟 도메인 타입과 id를 등록합니다.",
        content = [
            Content(
                encoding = [Encoding(name = "commandFileRequestVO", contentType = MediaType.APPLICATION_JSON_VALUE)],
            ),
        ],
        required = true,
    )
    @PostMapping("multi", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun postFiles(
        @RequestPart("files") files: List<MultipartFile>,
        @RequestPart("commandFileRequestVO")
        commandFileRequestVO: CommandFileRequestVO,
    ): ResponseDTO<Void> {
        commandFileService.uploadFiles(files, commandFileRequestVO)

        return ResponseDTO.ok(null)
    }

    @Operation(
        summary = "모임 게시글에 파일(다중) 등록",
        description = "모임 게시글에 파일(다중)을 등록합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "모임 게시글에 파일(다중) 등록 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @OpenApiBody(
        description = "추가로 모임 id를 등록합니다.",
        content = [
            Content(
                encoding = [Encoding(name = "commandFileRequestVO", contentType = MediaType.APPLICATION_JSON_VALUE)],
            ),
        ],
        required = true,
    )
    @PostMapping("/gathering", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun postFilesWithGatheringId(
        @RequestPart("files") files: List<MultipartFile>,
        @RequestPart("commandFileWithGatheringRequestVO") commandFileWithGatheringRequestVO: CommandFileWithGatheringRequestVO,
    ): ResponseDTO<Void> {
        commandFileService.uploadFilesWithGatheringId(files, commandFileWithGatheringRequestVO)

        return ResponseDTO.ok(null)
    }

    @Operation(
        summary = "프로필 사진 수정",
        description = "프로필 사진(1장)을 수정합니다. (회원, 모임)",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "프로필 사진 수정",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @OpenApiBody(
        description = "회원 id를 등록합니다.",
        content = [
            Content(
                encoding = [Encoding(name = "commandFileRequestVO", contentType = MediaType.APPLICATION_JSON_VALUE)],
            ),
        ],
        required = true,
    )
    @PatchMapping("/profile", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun patchProfileImage(
        @RequestPart("file") file: MultipartFile,
        @RequestPart("commandFileRequestVO") commandFileRequestVO: CommandFileRequestVO,
    ): ResponseDTO<Void> {
        commandFileService.updateProfileImage(file, commandFileRequestVO)

        return ResponseDTO.ok(null)
    }

    @Operation(
        summary = "파일(다중) 수정",
        description = "파일(다중)을 수정합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "파일(다중) 수정 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @OpenApiBody(
        description = "타겟 도메인 타입과 id를 등록합니다.",
        content = [
            Content(
                encoding = [
                    Encoding(name = "commandFileRequestVO", contentType = MediaType.APPLICATION_JSON_VALUE),
                    Encoding(name = "deleteFilesIds", contentType = MediaType.APPLICATION_JSON_VALUE),
                ],
            ),
        ],
        required = true,
    )
    @PatchMapping("", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun patchFiles(
        @RequestPart("commandFileRequestVO") commandFileRequestVO: CommandFileRequestVO,
        @RequestPart("deleteFilesIds") deleteFilesIds: List<Long>,
        @RequestPart("newFiles") newFiles: List<MultipartFile>,
    ): ResponseDTO<Void> {
        commandFileService.updateFiles(commandFileRequestVO, deleteFilesIds, newFiles)

        return ResponseDTO.ok(null)
    }

    @Operation(
        summary = "모임 게시글 파일(다중) 수정",
        description = "모임 게시글의 파일(다중)을 수정합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "모임 게시글 파일(다중) 수정 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @OpenApiBody(
        description = "모임 정보를 등록합니다.",
        content = [
            Content(
                encoding = [
                    Encoding(name = "commandFileRequestVO", contentType = MediaType.APPLICATION_JSON_VALUE),
                    Encoding(name = "deleteFilesIds", contentType = MediaType.APPLICATION_JSON_VALUE),
                ],
            ),
        ],
        required = true,
    )
    @PatchMapping("/gathering", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun patchFilesWithGatheringId(
        @RequestPart("commandFileRequestVO") commandFileWithGatheringRequestVO: CommandFileWithGatheringRequestVO,
        @RequestPart("deleteFilesIds") deleteFilesIds: List<Long>,
        @RequestPart("newFiles") newFiles: List<MultipartFile>,
    ): ResponseDTO<Void> {
        commandFileService.updateFilesWithGatheringId(commandFileWithGatheringRequestVO, deleteFilesIds, newFiles)

        return ResponseDTO.ok(null)
    }

    @Operation(
        summary = "파일(다중) 삭제",
        description = "파일(다중)을 삭제합니다.(모임 게시글 포함)",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "파일(다중) 삭제 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @DeleteMapping("")
    fun deleteFile(
        @RequestBody commandFileRequestVO: CommandFileRequestVO,
    ): ResponseDTO<Void> {
        commandFileService.deleteFile(commandFileRequestVO)
        return ResponseDTO.ok(null)
    }

    @GetMapping("/download/{fileName}")
    fun downloadFile(
        @PathVariable fileName: String,
    ): ResponseEntity<ByteArray> {
        val data = commandFileService.downloadFile(fileName)

        // 설명. 이거 responseDTO로 받을 수 있는 방법 알아보자
        return ResponseEntity
            .ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
            .body(data)
    }
}