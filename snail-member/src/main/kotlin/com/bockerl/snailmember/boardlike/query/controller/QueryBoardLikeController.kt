@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.boardlike.query.controller

import com.bockerl.snailmember.boardlike.query.service.QueryBoardLikeService
import com.bockerl.snailmember.common.ResponseDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/board-like")
class QueryBoardLikeController(
    private val queryBoardLikeService: QueryBoardLikeService,
) {
    @Operation(
        summary = "게시글 좋아요 회원 목록 조회",
        description =
            "게시글에 좋아요한 사람의 목록을 같이 반환합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 좋아요 회원 목록 조회 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @GetMapping("board/{boardId}")
    fun getBoardLike(
        @RequestParam(required = false) lastId: Long? = null,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @PathVariable boardId: String,
    ): ResponseDTO<*> {
        val queryBoardLikeResponseVO = queryBoardLikeService.readBoardLike(boardId, lastId, pageSize)

        return ResponseDTO.ok(queryBoardLikeResponseVO)
    }

    @Operation(
        summary = "게시글 좋아요 수 조회",
        description =
            "게시글 pk로 게시글 좋아요 수를 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 좋아요 수 조회 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @GetMapping("count/{boardId}")
    fun getBoardLikeCount(
        @PathVariable boardId: String,
    ): ResponseDTO<*> = ResponseDTO.ok(queryBoardLikeService.readBoardLikeCount(boardId))

    @Operation(
        summary = "회원의 좋아요한 게시글 조회",
        description =
            "회원의 좋아요한 게시글을 조회합니다..",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "회원의 좋아요한 게시글 조회",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @GetMapping("member/{memberId}")
    fun getBoardIdsByMemberId(
        @RequestParam(required = false) lastId: Long? = null,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @PathVariable memberId: String,
    ): ResponseDTO<*> = ResponseDTO.ok(queryBoardLikeService.readBoardIdsByMemberId(memberId, lastId, pageSize))
}