package com.bockerl.snailmember.board.query.controller

import com.bockerl.snailmember.board.command.application.dto.BoardDTO
import com.bockerl.snailmember.board.command.application.mapper.BoardConverter
import com.bockerl.snailmember.board.query.service.QueryBoardService
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
@RequestMapping("/api/board")
class QueryBoardController(
    private val queryBoardService: QueryBoardService,
    private val boardConverter: BoardConverter,
){
    @Operation(
        summary = "게시판 pk로 게시판 상세 조회",
        description = "게시판 pk로 게시판 ResponseVO를 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시판 조회 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = BoardDTO::class)),
                ],
            ),
        ],
    )
    @GetMapping("/{boardId}")
    fun getBoardByBoardId(
        @PathVariable boardId: Long,
        /* 궁금. 와일드 카드로 *를 쓸 것인지? */
    ): ResponseDTO<*> {
        val boardDTO: BoardDTO = queryBoardService.readBoardByBoardId(boardId)
        return ResponseDTO.ok(boardConverter.dtoToResponseVO(boardDTO))
    }
}