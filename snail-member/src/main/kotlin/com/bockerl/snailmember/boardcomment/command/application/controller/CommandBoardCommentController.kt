@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.boardcomment.command.application.controller

import com.bockerl.snailmember.boardcomment.command.application.dto.CommandBoardCommentCreateByGifDTO
import com.bockerl.snailmember.boardcomment.command.application.dto.CommandBoardCommentCreateDTO
import com.bockerl.snailmember.boardcomment.command.application.dto.CommandBoardCommentDeleteDTO
import com.bockerl.snailmember.boardcomment.command.application.service.CommandBoardCommentService
import com.bockerl.snailmember.boardcomment.command.domain.aggregate.vo.request.CommandBoardCommentCreateByGifRequestVO
import com.bockerl.snailmember.boardcomment.command.domain.aggregate.vo.request.CommandBoardCommentCreateRequestVO
import com.bockerl.snailmember.boardcomment.command.domain.aggregate.vo.request.CommandBoardCommentDeleteRequestVO
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
@RequestMapping("/api/board-comment")
class CommandBoardCommentController(
    private val commandBoardCommentService: CommandBoardCommentService,
) {
    @Operation(
        summary = "게시글 댓글 등록(내용만)",
        description =
            "입력된 게시글 댓글을 바탕으로 게시글을 등록합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 댓글 등록 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @PostMapping("contents")
    fun postBoardComment(
        @RequestHeader("idempotencyKey") idempotencyKey: String,
        @RequestBody commandBoardCommentCreateRequestVO: CommandBoardCommentCreateRequestVO,
    ): ResponseDTO<*> {
        val commandBoardCommentCreateDTO =
            CommandBoardCommentCreateDTO(
                boardCommentContents = commandBoardCommentCreateRequestVO.boardCommentContents,
                memberId = commandBoardCommentCreateRequestVO.memberId,
                boardId = commandBoardCommentCreateRequestVO.boardId,
                idempotencyKey = idempotencyKey,
            )
        commandBoardCommentService.createBoardComment(commandBoardCommentCreateDTO)

        return ResponseDTO.ok(null)
    }

    @Operation(
        summary = "게시글 댓글 등록(gif)",
        description =
            "입력된 게시글 댓글에 gif를 바탕으로 게시글을 등록합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 댓글 등록(gif) 성공",
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
                        name = "commandBoardCommentCreateByGifRequestVO",
                        contentType = MediaType.APPLICATION_JSON_VALUE,
                    ),
                ],
            ),
        ],
    )
    @PostMapping("gif", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun postBoardCommentByGif(
        @RequestHeader("idempotencyKey") idempotencyKey: String,
        @RequestPart("commandBoardCommentCreateByGifRequestVO") commandBoardCommentCreateByGifRequestVO:
            CommandBoardCommentCreateByGifRequestVO,
        @RequestPart("file", required = true) file: MultipartFile,
    ): ResponseDTO<*> {
        val commandBoardCommentCreateByGifDTO =
            CommandBoardCommentCreateByGifDTO(
                memberId = commandBoardCommentCreateByGifRequestVO.memberId,
                boardId = commandBoardCommentCreateByGifRequestVO.boardId,
                idempotencyKey = idempotencyKey,
            )

        commandBoardCommentService.createBoardCommentByGif(commandBoardCommentCreateByGifDTO, file)

        return ResponseDTO.ok(null)
    }

    @Operation(
        summary = "게시글 댓글 삭제",
        description =
            "게시글 댓글을 삭제합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 댓글 삭제 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @DeleteMapping("")
    fun deleteBoardComment(
        @RequestHeader("idempotencyKey") idempotencyKey: String,
        @RequestBody commandBoardCommentDeleteRequestVO: CommandBoardCommentDeleteRequestVO,
    ): ResponseDTO<*> {
        val commandBoardCommentDeleteDTO =
            CommandBoardCommentDeleteDTO(
                boardCommentId = commandBoardCommentDeleteRequestVO.boardCommentId,
                memberId = commandBoardCommentDeleteRequestVO.memberId,
                boardId = commandBoardCommentDeleteRequestVO.boardId,
                idempotencyKey = idempotencyKey,
            )
        commandBoardCommentService.deleteBoardComment(commandBoardCommentDeleteDTO)

        return ResponseDTO.ok(null)
    }
}