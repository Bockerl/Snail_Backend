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
                description = "게시판 pk로 게시판 상세 조회 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = BoardDTO::class)),
                ],
            ),
        ],
    )
    @GetMapping("/detail/{boardId}")
    fun getBoardByBoardId(
        @PathVariable boardId: Long,
        /* 궁금. 와일드 카드로 *를 쓸 것인지? */
    ): ResponseDTO<*> {
        val boardDTO: BoardDTO = queryBoardService.readBoardByBoardId(boardId)
//        return ResponseDTO.ok(boardConverter.dtoToResponseVO(boardDTO))
        return ResponseDTO.ok(boardDTO)
    }

    @Operation(
        summary = "게시판 타입으로 게시판 List 조회",
        description = "게시판 타입으로 게시판 ResponseVO List를 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시판 타입으로 게시판 List 조회 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = BoardDTO::class)),
                ],
            ),
        ],
    )
    @GetMapping("/{boardType}")
    fun getBoardByType(
        @PathVariable boardType: String,
    ): ResponseDTO<List<BoardDTO>> {
        val boardList: List<BoardDTO> = queryBoardService.readBoardByBoardType(boardType)
        
//        return ResponseDTO.ok(boardConverter.dtoToResponseVO(boardList))
        return ResponseDTO.ok(boardList)
    }
}