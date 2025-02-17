package com.bockerl.snailmember.boardcomment.query.service

import com.bockerl.snailmember.board.query.vo.QueryBoardResponseVO
import com.bockerl.snailmember.common.ResponseDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping

@Service
class QueryBoardCommentServiceImpl : QueryBoardCommentService {
    @Operation(
        summary = "개시글 pk로 해당 게시글 댓글 조회",
        description = "게시글 pk로 해당 게시글 댓글 List<ResponseVO>를 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "개시글 pk로 해당 게시글 댓글 조회 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = QueryBoardResponseVO::class)),
                ],
            ),
        ],
    )
    @GetMapping("/{boardId}")
    fun getBoardCommentByBoardId(): ResponseDTO<*> = ResponseDTO.ok(null)
}