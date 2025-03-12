package com.bockerl.snailmember.boardrecommentlike.command.application.controller

import com.bockerl.snailmember.boardrecommentlike.command.application.dto.CommandBoardRecommentLikeDTO
import com.bockerl.snailmember.boardrecommentlike.command.application.service.CommandBoardRecommentLikeService
import com.bockerl.snailmember.boardrecommentlike.command.domain.aggregate.vo.request.CommandBoardRecommentLikeRequestVO
import com.bockerl.snailmember.common.ResponseDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/board-recomment-like")
class CommandBoardRecommentLikeController(
    private val commandBoardRecommentLikeService: CommandBoardRecommentLikeService,
) {
    @Operation(
        summary = "게시글 대댓글 좋아요 등록",
        description = "좋아요 누른 회원 pk와 게시글,댓글,대댓글 pk를 바탕으로 게시글 댓글 좋아요를 등록합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 대댓글 좋아요 등록 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @PostMapping("")
    fun postBoardCommentLike(
        @RequestBody commandBoardRecommentLikeRequestVO: CommandBoardRecommentLikeRequestVO,
    ): ResponseDTO<*> {
        val commandBoardRecommentLikeDTO =
            CommandBoardRecommentLikeDTO(
                boardCommentId = commandBoardRecommentLikeRequestVO.boardCommentId,
                boardId = commandBoardRecommentLikeRequestVO.boardId,
                memberId = commandBoardRecommentLikeRequestVO.memberId,
                boardRecommentId = commandBoardRecommentLikeRequestVO.boardRecommentId,
            )
        commandBoardRecommentLikeService.createBoardRecommentLike(commandBoardRecommentLikeDTO)

        return ResponseDTO.ok(null)
    }

    @Operation(
        summary = "게시글 대댓글 좋아요 취소",
        description = "좋아요 게시글, 댓글, 대댓글 pk와 좋아요 누른 회원 pk를 바탕으로 게시글 댓글 좋아요를 취소합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 대댓글 좋아요 취소 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = ResponseDTO::class)),
                ],
            ),
        ],
    )
    @DeleteMapping("")
    fun deleteBoardRecommentLike(
        @RequestBody commandBoardRecommentLikeRequestVO: CommandBoardRecommentLikeRequestVO,
    ): ResponseDTO<*> {
        val commandBoardRecommentLikeDTO =
            CommandBoardRecommentLikeDTO(
                boardCommentId = commandBoardRecommentLikeRequestVO.boardCommentId,
                boardId = commandBoardRecommentLikeRequestVO.boardId,
                memberId = commandBoardRecommentLikeRequestVO.memberId,
                boardRecommentId = commandBoardRecommentLikeRequestVO.boardRecommentId,
            )

        commandBoardRecommentLikeService.deleteBoardRecommentLike(commandBoardRecommentLikeDTO)

        return ResponseDTO.ok(null)
    }
}