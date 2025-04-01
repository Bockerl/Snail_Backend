@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.boardrecomment.command.application.controller

import com.bockerl.snailmember.boardrecomment.command.application.dto.CommandBoardRecommentCreateByGifDTO
import com.bockerl.snailmember.boardrecomment.command.application.dto.CommandBoardRecommentCreateDTO
import com.bockerl.snailmember.boardrecomment.command.application.dto.CommandBoardRecommentDeleteDTO
import com.bockerl.snailmember.boardrecomment.command.application.service.CommandBoardRecommentService
import com.bockerl.snailmember.boardrecomment.command.domain.aggregate.vo.request.CommandBoardRecommentCreateByGifRequestVO
import com.bockerl.snailmember.boardrecomment.command.domain.aggregate.vo.request.CommandBoardRecommentCreateRequestVO
import com.bockerl.snailmember.boardrecomment.command.domain.aggregate.vo.request.CommandBoardRecommentDeleteRequestVO
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
@RequestMapping("/api/board-recomment")
class CommandBoardRecommentController(
    private val commandBoardRecommentService: CommandBoardRecommentService,
) {
    @Operation(
        summary = "게시글 대댓글 등록(내용만)",
        description =
            "입력된 게시글 대댓글을 바탕으로 게시글 대댓글을 등록합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 대댓글 등록 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @PostMapping("contents")
    fun postBoardComment(
        @RequestBody commandBoardRecommentCreateRequestVO: CommandBoardRecommentCreateRequestVO,
    ): ResponseDTO<*> {
        val commandBoardRecommentCreateDTO =
            CommandBoardRecommentCreateDTO(
                boardCommentContents = commandBoardRecommentCreateRequestVO.boardRecommentContents,
                memberId = commandBoardRecommentCreateRequestVO.memberId,
                boardId = commandBoardRecommentCreateRequestVO.boardId,
                boardCommentId = commandBoardRecommentCreateRequestVO.boardCommentId,
            )
        commandBoardRecommentService.createBoardRecomment(commandBoardRecommentCreateDTO)

        return ResponseDTO.ok(null)
    }

    @Operation(
        summary = "게시글 대댓글 등록(gif)",
        description =
            "입력된 게시글 대댓글에 gif를 바탕으로 게시글 대댓글을 등록합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 대댓글 등록(gif) 성공",
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
                        name = "commandBoardRecommentCreateByGifRequestVO",
                        contentType = MediaType.APPLICATION_JSON_VALUE,
                    ),
                ],
            ),
        ],
    )
    @PostMapping("gif", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun postBoardCommentByGif(
        @RequestHeader("idempotencyKey") idempotencyKey: String,
        @RequestPart("commandBoardRecommentCreateByGifRequestVO") commandBoardRecommentCreateByGifRequestVO:
            CommandBoardRecommentCreateByGifRequestVO,
        @RequestPart("file", required = true) file: MultipartFile,
    ): ResponseDTO<*> {
        val commandBoardRecommentCreateByGifDTO =
            CommandBoardRecommentCreateByGifDTO(
                memberId = commandBoardRecommentCreateByGifRequestVO.memberId,
                boardId = commandBoardRecommentCreateByGifRequestVO.boardId,
                boardCommentId = commandBoardRecommentCreateByGifRequestVO.boardCommentId,
                idempotencyKey = idempotencyKey,
            )

        commandBoardRecommentService.createBoardRecommentByGif(commandBoardRecommentCreateByGifDTO, file)

        return ResponseDTO.ok(null)
    }

    @Operation(
        summary = "게시글 대댓글 삭제",
        description =
            "게시글 대댓글을 삭제합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 대댓글 삭제 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @DeleteMapping("")
    fun deleteBoardComment(
        @RequestHeader("idempotencyKey") idempotencyKey: String,
        @RequestBody commandBoardRecommentDeleteRequestVO: CommandBoardRecommentDeleteRequestVO,
    ): ResponseDTO<*> {
        val commandBoardRecommentDeleteDTO =
            CommandBoardRecommentDeleteDTO(
                boardCommentId = commandBoardRecommentDeleteRequestVO.boardCommentId,
                memberId = commandBoardRecommentDeleteRequestVO.memberId,
                boardId = commandBoardRecommentDeleteRequestVO.boardId,
                boardRecommentId = commandBoardRecommentDeleteRequestVO.boardRecommentId,
                idempotencyKey = idempotencyKey,
            )
        commandBoardRecommentService.deleteBoardRecomment(commandBoardRecommentDeleteDTO)

        return ResponseDTO.ok(null)
    }
}