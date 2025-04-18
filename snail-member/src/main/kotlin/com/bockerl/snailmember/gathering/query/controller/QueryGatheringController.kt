package com.bockerl.snailmember.gathering.query.controller

import com.bockerl.snailmember.common.ResponseDTO
import com.bockerl.snailmember.gathering.command.application.mapper.GatheringConverter
import com.bockerl.snailmember.gathering.query.dto.QueryGatheringResponseDTO
import com.bockerl.snailmember.gathering.query.service.QueryGatheringService
import com.bockerl.snailmember.gathering.query.vo.QueryGatheringResponseVO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/gathering")
class QueryGatheringController(
    private val queryGatheringService: QueryGatheringService,
    private val gatheringConverter: GatheringConverter,
) {
    @Operation(
        summary = "모임 pk로 모임 상세 조회",
        description = "모임 pk로 모임 정보, 사진첩, 일정, 게시글을 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "모임 pk로 모임 상세 조회 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = QueryGatheringResponseVO::class)),
                ],
            ),
        ],
    )
    @GetMapping("/detail/{gatheringId}")
    fun getGatheringByGatheringId(
        @PathVariable gatheringId: String,
    ): ResponseDTO<*> {
        val queryGatheringResponseDTO: QueryGatheringResponseDTO = queryGatheringService.readGatheringByGatheringId(gatheringId)

        // 상세조회는 다른 api 구현 후 하기
        // 일정, 사진첩, 게시글을 하나에 보여줘야함

//        return ResponseDTO.ok(gatheringConverter.dtoToResponseVO(gatheringDTO))
        // 변환해야함
        return ResponseDTO.ok(queryGatheringResponseDTO)
    }

    @Operation(
        summary = "모임 전체 조회",
        description = "모임 전체를 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = " 모임 전체 조회 성공",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = QueryGatheringResponseVO::class)),
                ],
            ),
        ],
    )
    @GetMapping("")
    fun getGathering(
        @RequestParam(required = false) lastId: Long? = null,
        @RequestParam(defaultValue = "10") pageSize: Int,
    ): ResponseDTO<List<QueryGatheringResponseVO>> {
        val gatheringList: List<QueryGatheringResponseVO> = queryGatheringService.readGathering(lastId, pageSize)

//        return ResponseDTO.ok(boardConverter.dtoToResponseVO(boardList))
        return ResponseDTO.ok(gatheringList)
    }
}