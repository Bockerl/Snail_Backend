package com.bockerl.snailmember.boardcommentlike.query.controller

import com.bockerl.snailmember.boardcommentlike.query.service.QueryBoardCommentLikeService
import com.bockerl.snailmember.common.ResponseDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/board-comment-like")
class QueryBoardCommentLikeController(
    private val queryBoardCommentLikeService: QueryBoardCommentLikeService,
) {
    @Operation(
        summary = "게시글 댓글 좋아요 수 조회",
        description =
            "게시글 댓글 pk로 게시글 댓글 좋아요 수를 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 댓글 좋아요 수 조회 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @GetMapping("count/{boardCommentId}")
    fun getBoardCommentLikeCount(
        @PathVariable boardCommentId: String,
    ): ResponseDTO<*> = ResponseDTO.ok(queryBoardCommentLikeService.readBoardCommentLikeCount(boardCommentId))
}