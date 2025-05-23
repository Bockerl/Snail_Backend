/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */

@file:Suppress("ktlint:standard:import-ordering", "ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.board.command.application.controller

import com.bockerl.snailmember.board.command.application.dto.CommandBoardCreateDTO
import com.bockerl.snailmember.board.command.application.dto.CommandBoardDeleteDTO
import com.bockerl.snailmember.board.command.application.dto.CommandBoardUpdateDTO
import com.bockerl.snailmember.board.command.application.service.CommandBoardService
import com.bockerl.snailmember.board.command.domain.aggregate.vo.request.CommandBoardCreateRequestVO
import com.bockerl.snailmember.board.command.domain.aggregate.vo.request.CommandBoardDeleteRequestVO
import com.bockerl.snailmember.board.command.domain.aggregate.vo.request.CommandBoardUpdateRequestVO
// import com.bockerl.snailmember.board.command.domain.aggregate.vo.response.BoardResponseVO
import com.bockerl.snailmember.common.ResponseDTO
import com.bockerl.snailmember.infrastructure.config.OpenApiBody
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Encoding
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/board")
class CommandBoardController(
    private val commandBoardService: CommandBoardService,
) {
    @Operation(
        summary = "게시글 등록",
        description =
            """
            입력된 게시글 정보와 파일을 바탕으로 게시글을 등록합니다.
            파일은 선택 사항으로 넣지 않아도 됩니다.
        """,
    )
    // 설명. api response 스키마 바꾸겠습니다.
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 등록 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
                // 궁금. cud response는 schema 설정을 다르게 해줘야 하지 않을까..
            ),
        ],
    )
    @OpenApiBody(
        content = [
            Content(
                encoding = [
                    Encoding(
                        name = "commandBoardRequestVO",
                        contentType = MediaType.APPLICATION_JSON_VALUE,
                    ),
                ],
            ),
        ],
    )
    @PostMapping("", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun postBoard(
        @RequestHeader("idempotencyKey") idempotencyKey: String,
        @RequestPart("commandBoardRequestVO") commandBoardCreateRequestVO: CommandBoardCreateRequestVO,
        @RequestPart("files", required = false) files: List<MultipartFile>?,
    ): ResponseDTO<*> {
        val commandBoardCreateDTO =
            CommandBoardCreateDTO(
                boardContents = commandBoardCreateRequestVO.boardContents,
                boardType = commandBoardCreateRequestVO.boardType,
                boardTag = commandBoardCreateRequestVO.boardTag,
                boardLocation = commandBoardCreateRequestVO.boardLocation,
                boardAccessLevel = commandBoardCreateRequestVO.boardAccessLevel,
                memberId = commandBoardCreateRequestVO.memberId,
                idempotencyKey = idempotencyKey,
            )

        commandBoardService.createBoard(commandBoardCreateDTO, files ?: emptyList())

        return ResponseDTO.ok(null)
    }

    @Operation(
        summary = "게시글 수정",
        description =
            """
            입력된 게시글 정보와 파일을 바탕으로 게시글을 수정합니다.
            파일은 선택 사항으로 넣지 않아도 됩니다.
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 수정 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @OpenApiBody(
        content = [
            Content(
                encoding = [
                    Encoding(
                        name = "commandBoardUpdateRequestVO",
                        contentType = MediaType.APPLICATION_JSON_VALUE,
                    ),
                ],
            ),
        ],
    )
    @PatchMapping("", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun patchBoard(
        @RequestHeader("idempotencyKey") idempotencyKey: String,
        @RequestPart("commandBoardUpdateRequestVO") commandBoardUpdateRequestVO: CommandBoardUpdateRequestVO,
        @RequestPart("files", required = false) files: List<MultipartFile>?,
    ): ResponseDTO<*> {
        val commandBoardUpdateDTO =
            CommandBoardUpdateDTO(
                boardId = commandBoardUpdateRequestVO.boardId,
                boardContents = commandBoardUpdateRequestVO.boardContents,
                boardType = commandBoardUpdateRequestVO.boardType,
                boardTag = commandBoardUpdateRequestVO.boardTag,
                boardLocation = commandBoardUpdateRequestVO.boardLocation,
                boardAccessLevel = commandBoardUpdateRequestVO.boardAccessLevel,
                memberId = commandBoardUpdateRequestVO.memberId,
                deleteFilesIds = commandBoardUpdateRequestVO.deleteFilesIds,
                idempotencyKey = idempotencyKey,
            )

        commandBoardService.updateBoard(commandBoardUpdateDTO, files ?: emptyList())

        return ResponseDTO.ok(null)
    }

    @Operation(
        summary = "게시글 삭제",
        description =
            """
            게시글 번호와 회원 번호를 이용하여 삭제합니다.
            회원 번호는 검증 용도로 사용됩니다.(추후 로직 상에서 구현 예정)
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 삭제 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @DeleteMapping("")
    fun deleteBoard(
        @RequestHeader("idempotencyKey") idempotencyKey: String,
        @RequestBody commandBoardDeleteRequestVO: CommandBoardDeleteRequestVO,
    ): ResponseDTO<*> {
        val commandBoardDeleteDTO =
            CommandBoardDeleteDTO(
                boardId = commandBoardDeleteRequestVO.boardId,
                memberId = commandBoardDeleteRequestVO.memberId,
                idempotencyKey = idempotencyKey,
            )

        commandBoardService.deleteBoard(commandBoardDeleteDTO)

        return ResponseDTO.ok(null)
    }
}