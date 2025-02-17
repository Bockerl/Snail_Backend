@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.boardcomment.query.controller

import com.bockerl.snailmember.boardcomment.query.service.QueryBoardCommentService
import com.bockerl.snailmember.boardcomment.query.vo.QueryBoardCommentResponseVO
import com.bockerl.snailmember.common.ResponseDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/board-comment")
class QueryBoardCommentController(
    private val queryBoardCommentService: QueryBoardCommentService,
) {
    @Operation(
        summary = "게시글 pk로 해당 게시글 댓글 조회",
        description = "게시글 pk로 해당 게시글 댓글 List<ResponseVO>를 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 pk로 해당 게시글 댓글 조회 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = QueryBoardCommentResponseVO::class)),
                ],
            ),
        ],
    )
    @GetMapping("/{boardId}")
    fun getBoardCommentByBoardId(
        @PathVariable boardId: String,
        @RequestParam(required = false) lastId: Long? = null,
        @RequestParam(defaultValue = "10") pageSize: Int,
    ): ResponseDTO<*> {
        val boardCommentList = queryBoardCommentService.getBoardCommentByBoardId(boardId, lastId, pageSize)

        return ResponseDTO.ok(boardCommentList)
    }
}