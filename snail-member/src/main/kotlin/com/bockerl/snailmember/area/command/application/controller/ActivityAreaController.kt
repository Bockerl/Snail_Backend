package com.bockerl.snailmember.area.command.application.controller

import com.bockerl.snailmember.area.command.application.mapper.AreaConverter
import com.bockerl.snailmember.area.command.application.service.CommandAreaService
import com.bockerl.snailmember.common.ResponseDTO
import com.bockerl.snailmember.member.command.domain.vo.request.ActivityAreaRequestVO
import com.bockerl.snailmember.security.config.CurrentMemberId
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/activity_area")
class ActivityAreaController(
    private val commandAreaService: CommandAreaService,
    private val areaConverter: AreaConverter,
) {
    private val logger = KotlinLogging.logger {}

    @Operation(
        summary = "활동지역 등록",
        description = """
        계정의 주 지역과 직장 지역을 등록합니다.
        주 지역은 필수이고, 직장 지역은 선택입니다.
    """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "활동지역 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ResponseDTO::class),
                    ),
                ],
            ),
        ],
    )
    @PostMapping("/activity_area")
    fun postActivityArea(
        @RequestHeader("idempotencyKey") idempotencyKey: String,
        @RequestBody requestVO: ActivityAreaRequestVO,
        @Parameter(hidden = true)
        @CurrentMemberId memberId: String,
    ): ResponseDTO<*> {
        logger.info { "활동지역 설정 요청 controller에 도착" }
        val requestDTO = areaConverter.activityAreaRequestVOToDTO(requestVO)
        commandAreaService.postActivityArea(memberId, requestDTO, idempotencyKey)
        return ResponseDTO.ok("활동지역 설정 성공")
    }
}