/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.boardlike.command.application.controller

import com.bockerl.snailmember.boardlike.command.application.dto.CommandBoardLikeDTO
import com.bockerl.snailmember.boardlike.command.application.service.CommandBoardLikeService
import com.bockerl.snailmember.boardlike.command.domain.aggregate.vo.request.CommandBoardLikeRequestVO
import com.bockerl.snailmember.common.ResponseDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/board-like")
class CommandBoardLikeController(
    private val commandBoardLikeService: CommandBoardLikeService,
) {
    @Operation(
        summary = "게시글 좋아요 등록",
        description = "좋아요 게시글 pk와 좋아요 누른 회원 pk를 바탕으로 게시글 좋아요를 등록합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 좋아요 등록 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @PostMapping("")
    fun postBoardLike(
        @RequestBody commandBoardLikeRequestVO: CommandBoardLikeRequestVO,
    ): ResponseDTO<*> {
        val commandBoardLikeDTO =
            CommandBoardLikeDTO(
                boardId = commandBoardLikeRequestVO.boardId,
                memberId = commandBoardLikeRequestVO.memberId,
                idempotencyKey = commandBoardLikeRequestVO.idempotencyKey,
            )

        commandBoardLikeService.createBoardLike(commandBoardLikeDTO)

        return ResponseDTO.ok(null)
    }

    @Operation(
        summary = "게시글 좋아요 취소",
        description = "좋아요 게시글 pk와 좋아요 누른 회원 pk를 바탕으로 게시글 좋아요를 취소합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 좋아요 취소 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @DeleteMapping("")
    fun deleteBoardLike(
        @RequestBody commandBoardLikeRequestVO: CommandBoardLikeRequestVO,
    ): ResponseDTO<*> {
        val commandBoardLikeDTO =
            CommandBoardLikeDTO(
                boardId = commandBoardLikeRequestVO.boardId,
                memberId = commandBoardLikeRequestVO.memberId,
                idempotencyKey = commandBoardLikeRequestVO.idempotencyKey,
            )
        commandBoardLikeService.deleteBoardLike(commandBoardLikeDTO)

        return ResponseDTO.ok(null)
    }
}