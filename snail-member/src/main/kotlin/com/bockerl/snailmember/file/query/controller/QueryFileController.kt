package com.bockerl.snailmember.file.query.controller

import com.bockerl.snailmember.board.command.domain.aggregate.vo.response.BoardResponseVO
import com.bockerl.snailmember.board.query.vo.QueryBoardResponseVO
import com.bockerl.snailmember.common.ResponseDTO
import com.bockerl.snailmember.file.query.service.QueryFileService
import com.bockerl.snailmember.file.query.vo.request.QueryFileRequestVO
import com.bockerl.snailmember.file.query.vo.response.QueryFileResponseVO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/file")
class QueryFileController(
    private val queryFileService: QueryFileService,
) {
    @Operation(
        summary = "타겟 도메인 타입, pk로 해당 파일 조회",
        description = "타겟 도메인 타입, pk로 해당 파일을 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "타겟 도메인 타입, pk로 해당 파일을 조회 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = QueryFileResponseVO::class)),
                ],
            ),
        ],
    )
    @PostMapping("/target")
    fun getFilesByTarget(
        @RequestBody queryFileRequestVO: QueryFileRequestVO,
    ): ResponseDTO<*> {

        val fileList = queryFileService.readFilesByTarget(queryFileRequestVO)

        return ResponseDTO.ok(fileList)
    }

    @Operation(
        summary = "모임별 전체 사진 조회",
        description = "모임별 전체 사진을 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "모임별 전체 사진 조회성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = QueryFileResponseVO::class)),
                ],
            ),
        ],
    )
    @GetMapping("/gathering/{gatheringId}")
    fun getFilesByGatheringId(
        @PathVariable("gatheringId") gatheringId: Long,
    ): ResponseDTO<*> {

        val fileList = queryFileService.readFilesByGatheringId(gatheringId)

        return ResponseDTO.ok(fileList)
    }

//    @Operation(
//        summary = "프로필 사진 조회",
//        description = "프로필 사진을 조회합니다.",
//    )
//    @ApiResponses(
//        value = [
//            ApiResponse(
//                responseCode = "200",
//                description = "프로필 사진 조회 성공",
//                content = [
//                    Content(mediaType = "application/json", schema = Schema(implementation = QueryFileResponseVO::class)),
//                ],
//            ),
//        ],
//    )
//    fun getProfileImage(
//        @RequestBody queryFileRequestVO: QueryFileRequestVO,
//    ): ResponseDTO<*> {
//
//        val fileList = queryFileService.readFilesByTarget(queryFileRequestVO)
//
//        return ResponseDTO.ok(fileList)
//    }


}