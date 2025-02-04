package com.bockerl.snailmember.board.query.controller

import com.bockerl.snailmember.board.command.application.dto.BoardDTO
import com.bockerl.snailmember.board.command.application.mapper.BoardConverter
import com.bockerl.snailmember.board.command.domain.aggregate.vo.response.BoardResponseVO
import com.bockerl.snailmember.board.query.service.QueryBoardService
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
    private val boardConverter: BoardConverter,
){

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
                    Content(mediaType = "application/json", schema = Schema(implementation = BoardResponseVO::class)),
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
        summary = "게시글 타입으로 게시글 List 조회",
        description = "게시글 타입으로 게시글 List를 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 타입으로 게시글 List 조회 성공",
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
                    Content(mediaType = "application/json", schema = Schema(implementation = BoardDTO::class)),
                ],
            ),
        ],
    )
    @PostMapping("/tag")
    fun getBoardByTag(
        @RequestBody boardTagList: List<String>,
    ): ResponseDTO<List<BoardDTO>> {
        val boardList: List<BoardDTO> = queryBoardService.readBoardByBoardTag(boardTagList)

        return ResponseDTO.ok(boardList)
    }

}