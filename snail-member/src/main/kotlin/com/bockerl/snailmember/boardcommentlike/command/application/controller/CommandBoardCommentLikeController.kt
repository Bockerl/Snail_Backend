/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.boardcommentlike.command.application.controller

import com.bockerl.snailmember.boardcommentlike.command.application.dto.CommandBoardCommentLikeDTO
import com.bockerl.snailmember.boardcommentlike.command.application.service.CommandBoardCommentLikeService
import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.vo.request.CommandBoardCommentLikeRequestVO
import com.bockerl.snailmember.common.ResponseDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/board-comment-like")
class CommandBoardCommentLikeController(
    private val commandBoardCommentLikeService: CommandBoardCommentLikeService,
) {
    @Operation(
        summary = "게시글 댓글 좋아요 등록",
        description = "좋아요 게시글 pk와 좋아요 누른 회원 pk 좋아요 게시글 댓글 pk를 바탕으로 게시글 댓글 좋아요를 등록합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 댓글 좋아요 등록 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @PostMapping("")
    fun postBoardCommentLike(
        @RequestBody commandBoardCommentLikeRequestVO: CommandBoardCommentLikeRequestVO,
    ): ResponseDTO<*> {
        val commandBoardCommentLikeDTO =
            CommandBoardCommentLikeDTO(
                boardCommentId = commandBoardCommentLikeRequestVO.boardCommentId,
                boardId = commandBoardCommentLikeRequestVO.boardId,
                memberId = commandBoardCommentLikeRequestVO.memberId,
            )
        commandBoardCommentLikeService.createBoardCommentLike(commandBoardCommentLikeDTO)

        return ResponseDTO.ok(null)
    }

    @Operation(
        summary = "게시글 댓글 좋아요 취소",
        description = "좋아요 게시글 pk와 좋아요 누른 회원 pk 좋아요 게시글 댓글 pk를 바탕으로 게시글 댓글 좋아요를 취소합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 댓글 좋아요 취소 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @DeleteMapping("")
    fun deleteBoardCommentLike(
        @RequestBody commandBoardCommentLikeRequestVO: CommandBoardCommentLikeRequestVO,
    ): ResponseDTO<*> {
        val commandBoardCommentLikeDTO =
            CommandBoardCommentLikeDTO(
                boardCommentId = commandBoardCommentLikeRequestVO.boardCommentId,
                boardId = commandBoardCommentLikeRequestVO.boardId,
                memberId = commandBoardCommentLikeRequestVO.memberId,
            )

        commandBoardCommentLikeService.deleteBoardCommentLike(commandBoardCommentLikeDTO)

        return ResponseDTO.ok(null)
    }

//    @Operation(
//        summary = "게시글 댓글 좋아요 수 조회",
//        description =
//            "게시글 댓글 pk로 게시글 좋아요 수를 조회합니다.",
//    )
//    @ApiResponses(
//        value = [
//            ApiResponse(
//                responseCode = "200",
//                description = "게시글 댓글 좋아요 수 조회 성공",
//                content = [
//                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
//                ],
//            ),
//        ],
//    )
//    @GetMapping("count/{boardId}")
//    fun getBoardCommentLikeCount(
//        @PathVariable boardId: String,
//    ): ResponseDTO<*> = ResponseDTO.ok(commandBoardCommentLikeService.readBoardCommentLikeCount(boardId))
}