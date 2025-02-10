package com.bockerl.snailmember.board.command.application.controller

import com.bockerl.snailmember.board.command.application.service.CommandBoardService
import com.bockerl.snailmember.board.command.domain.aggregate.vo.request.CommandBoardRequestVO
import com.bockerl.snailmember.board.command.domain.aggregate.vo.request.CommandBoardUpdateRequestVO
import com.bockerl.snailmember.board.command.domain.aggregate.vo.response.BoardResponseVO
import com.bockerl.snailmember.common.ResponseDTO
import com.bockerl.snailmember.config.OpenApiBody
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Encoding
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/board")
class CommandBoardController(
    private val commandBoardService: CommandBoardService,
)
{

    @Operation(
        summary = "게시글 등록",
        description = "입력된 게시글 정보와 파일을 바탕으로 게시글을 등록합니다." +
            "파일은 선택 사항으로 넣지 않아도 됩니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 등록 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = BoardResponseVO::class)),
                ],
                /* 궁금. cud response는 schema 설정을 다르게 해줘야 하지 않을까.. */
            ),
        ],
    )
    @OpenApiBody(
        content = [
            Content(
                encoding = [Encoding(name = "commandBoardRequestVO",
                    contentType = MediaType.APPLICATION_JSON_VALUE)]
            )
        ],
    )
    @PostMapping("", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun postBoard(
        @RequestPart("commandBoardRequestVO") commandBoardRequestVO: CommandBoardRequestVO,
        @RequestPart("files", required = false) files: List<MultipartFile>?,
        ): ResponseDTO<BoardResponseVO> {

        commandBoardService.createBoard(commandBoardRequestVO, files?: emptyList())

        return ResponseDTO.ok(null);
    }

    @Operation(
        summary = "게시글 수정",
        description = "입력된 게시글 정보와 파일을 바탕으로 게시글을 수정합니다." +
            "파일은 선택 사항으로 넣지 않아도 됩니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "게시글 수정 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = BoardResponseVO::class)),
                ],
            ),
        ],
    )
    @OpenApiBody(
        content = [
            Content(
                encoding = [Encoding(name = "commandBoardRequestVO",
                    contentType = MediaType.APPLICATION_JSON_VALUE)]
            )
        ],
    )
    @PatchMapping("", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun patchBoard(
        @RequestPart("commandBoardUpdateRequestVO") commandBoardUpdateRequestVO: CommandBoardUpdateRequestVO,
        @RequestPart("files", required = false) files: List<MultipartFile>?,
    ): ResponseDTO<BoardResponseVO> {

        commandBoardService.updateBoard(commandBoardUpdateRequestVO, files?: emptyList())


        return ResponseDTO.ok(null);
    }

}