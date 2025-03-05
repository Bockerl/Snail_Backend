@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.boardrecomment.query.controller

import com.bockerl.snailmember.boardrecomment.query.service.QueryBoardRecommentService
import com.bockerl.snailmember.boardrecomment.query.vo.QueryBoardRecommentResponseVO
import com.bockerl.snailmember.common.ResponseDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/board-recomment")
class QueryBoardRecommentController(
    private val queryBoardRecommentService: QueryBoardRecommentService,
) {
    @Operation(
        summary = "게시글 댓글 pk로 해당 게시글 대댓글 조회",
        description = "게시글 댓글 pk로 해당 게시글 대댓글 List<ResponseVO>를 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 댓글 pk로 해당 게시글 대댓글 조회 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = QueryBoardRecommentResponseVO::class)),
                ],
            ),
        ],
    )
    @GetMapping("board/{boardCommentId}")
    fun getBoardRecommentByBoardCommentId(
        @PathVariable boardCommentId: String,
        @RequestParam(required = false) lastId: Long? = null,
        @RequestParam(defaultValue = "10") pageSize: Int,
    ): ResponseDTO<*> {
        val boardRecommentList = queryBoardRecommentService.getBoardRecommentByBoardCommentId(boardCommentId, lastId, pageSize)

        return ResponseDTO.ok(boardRecommentList)
    }

    @Operation(
        summary = "내 대댓글 목록 조회",
        description = "회원 pk로 내 대댓글들 List<ResponseVO>를 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "내 대댓글 목록 조회 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = QueryBoardRecommentResponseVO::class)),
                ],
            ),
        ],
    )
    @GetMapping("member/{memberId}")
    fun getBoardCommentByMemberId(
        @PathVariable memberId: String,
        @RequestParam(required = false) lastId: Long? = null,
        @RequestParam(defaultValue = "10") pageSize: Int,
    ): ResponseDTO<*> {
        val boardRecommentList = queryBoardRecommentService.getBoardRecommentByMemberId(memberId, lastId, pageSize)

        return ResponseDTO.ok(boardRecommentList)
    }
}