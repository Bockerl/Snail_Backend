/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */

@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.board.query.controller

// import com.bockerl.snailmember.board.command.domain.aggregate.vo.response.BoardResponseVO
import com.bockerl.snailmember.board.query.service.QueryBoardService
import com.bockerl.snailmember.board.query.vo.QueryBoardResponseVO
import com.bockerl.snailmember.common.ResponseDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/board")
class QueryBoardController(
    private val queryBoardService: QueryBoardService,
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @Operation(
        summary = "게시글 pk로 게시글 상세 조회",
        description = "게시글 pk로 게시글 ResponseVO를 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 pk로 게시판 상세 조회 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = QueryBoardResponseVO::class)),
                ],
            ),
        ],
    )
    @GetMapping("/detail/{boardId}")
    fun getBoardByBoardId(
        @PathVariable boardId: String,
        // 궁금. 와일드 카드로 *를 쓸 것인지?
    ): ResponseDTO<*> {
        val queryBoardResponseVO: QueryBoardResponseVO = queryBoardService.readBoardByBoardId(boardId)

//        return ResponseDTO.ok(boardConverter.dtoToResponseVO(boardDTO))
        return ResponseDTO.ok(queryBoardResponseVO)
    }

    @Operation(
        summary = "게시글 타입으로 게시글 List 조회",
        description = "게시글 타입으로 게시글 List를 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 타입으로 게시글 List 조회 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = QueryBoardResponseVO::class)),
                ],
            ),
        ],
    )
    @GetMapping("/{boardType}")
    fun getBoardByType(
        @PathVariable boardType: String,
    ): ResponseDTO<List<QueryBoardResponseVO>> {
        val boardList: List<QueryBoardResponseVO> = queryBoardService.readBoardByBoardType(boardType)

//        return ResponseDTO.ok(boardConverter.dtoToResponseVO(boardList))
        return ResponseDTO.ok(boardList)
    }

    @Operation(
        summary = "게시글 태그로 게시글 List 조회",
        description = "게시글 태그로 게시글 List를 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시판 태그로 게시판 List 조회 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = QueryBoardResponseVO::class)),
                ],
            ),
        ],
    )
    @PostMapping("/tag")
    fun getBoardByTag(
        @RequestBody boardTagList: List<String>,
    ): ResponseDTO<List<QueryBoardResponseVO>> {
        val boardList: List<QueryBoardResponseVO> = queryBoardService.readBoardByBoardTag(boardTagList)

        return ResponseDTO.ok(boardList)
    }
}