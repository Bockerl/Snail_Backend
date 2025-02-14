/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.boardlike.command.application.controller

import com.bockerl.snailmember.boardlike.command.application.service.CommandBoardLikeService
import com.bockerl.snailmember.boardlike.command.domain.vo.request.CommandBoardLikeCreateRequestVO
import com.bockerl.snailmember.common.ResponseDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/board_like")
class CommandBoardLikeController(
    private val commandBoardLikeService: CommandBoardLikeService,
) {
    @Operation(
        summary = "게시글 등록",
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
        @RequestBody commandBoardLikeCreateRequestVO: CommandBoardLikeCreateRequestVO,
    ): ResponseDTO<*> {
        commandBoardLikeService.createBoardLike(commandBoardLikeCreateRequestVO)

        return ResponseDTO.ok(null)
    }
}