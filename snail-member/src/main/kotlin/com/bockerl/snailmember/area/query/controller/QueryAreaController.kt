package com.bockerl.snailmember.area.query.controller

import com.bockerl.snailmember.area.command.application.mapper.AreaConverter
import com.bockerl.snailmember.area.query.service.QueryAreaService
import com.bockerl.snailmember.area.query.vo.request.AreaKeywordRequestVO
import com.bockerl.snailmember.area.query.vo.response.AreaKeywordResponseVO
import com.bockerl.snailmember.common.ResponseDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/area")
class QueryAreaController(
    private val queryAreaService: QueryAreaService,
    private val areaConverter: AreaConverter,
) {
    @Operation(
        summary = "키워드 기반 동네 검색",
        description = "키워드를 바탕으로 최소 군구, 최대 읍면동 단위의 동네를 검색합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "키워드 기반 동네 검색 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = AreaKeywordResponseVO::class),
                    ),
                ],
            ),
        ],
    )
    @GetMapping("/keyword")
    fun getAreaByKeyword(
        @RequestParam("area_search_keyword") keyword: String,
    ): ResponseDTO<*> {
        // 유효성 검사
        val requestVO = AreaKeywordRequestVO(keyword)
        requestVO.apply {
            validatedKeyword()
        }
        // DTO 변환
        val requestDTO = areaConverter.areaKeywordRequestVOToDTO(requestVO)
        // 서비스 결과 DTO
        val responseDTO = queryAreaService.selectAreaByKeyword(requestDTO)
        // VO 변환
        val responseVO = areaConverter.areaKeywordResponseDTOToVO(responseDTO)
        return ResponseDTO.ok(responseVO)
    }
}