package com.bockerl.snailmember.board.command.application.controller

import com.bockerl.snailmember.board.command.application.service.CommandBoardService
import com.bockerl.snailmember.board.command.domain.aggregate.vo.request.BoardRequestVO
import com.bockerl.snailmember.board.command.domain.aggregate.vo.response.BoardResponseVO
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
@RequestMapping("/api/board")
class CommandBoardController(
    private val commandBoardService: CommandBoardService,
)
{

//    @Operation(
//        summary = "게시글 등록",
//        description = "입력된 게시글 정보를 바탕으로 게시글을 등록합니다.",
//    )
//    @ApiResponses(
//        value = [
//            ApiResponse(
//                responseCode = "200",
//                description = "게시글 등록 성공",
//                content = [
//                    Content(mediaType = "application/json", schema = Schema(implementation = BoardResponseVO::class)),
//                ],
//                /* 궁금. cud response는 schema 설정을 다르게 해줘야 하지 않을까.. */
//            ),
//        ],
//    )
//    @PostMapping("")
//    fun postBoard(
//        @RequestBody boardRequest: BoardRequestVO,
//
//    ): ResponseDTO<BoardResponseVO> {
//
//    }
}