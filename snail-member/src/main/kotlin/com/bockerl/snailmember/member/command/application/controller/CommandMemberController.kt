/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.member.command.application.controller

import com.bockerl.snailmember.common.ResponseDTO
import com.bockerl.snailmember.member.command.application.mapper.MemberConverter
import com.bockerl.snailmember.member.command.application.service.CommandMemberService
import com.bockerl.snailmember.member.command.domain.vo.request.ActivityAreaRequestVO
import com.bockerl.snailmember.member.command.domain.vo.request.ProfileRequestVO
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/member")
class CommandMemberController(
    private val commandMemberService: CommandMemberService,
    private val memberConverter: MemberConverter,
) {
    private val logger = KotlinLogging.logger {}

    @Operation(
        summary = "활동지역 등록",
        description = "계정의 주 지역과 직장 지역을 등록합니다.",
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
        @RequestBody requestVO: ActivityAreaRequestVO,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseDTO<*> {
        logger.info { "활동지역 설정 요청 controller에 도착" }
        val memberId: String = jwt.getClaimAsString("memberId")
        logger.info { "controller에 도착한 memberId: $memberId" }
        val requestDTO = memberConverter.activityAreaRequestVOToDTO(requestVO)
        commandMemberService.postActivityArea(memberId, requestDTO)
        return ResponseDTO.ok("활동지역 설정 성공")
    }

    @PatchMapping("/profile")
    fun patchProfile(
        @RequestBody requestVO: ProfileRequestVO,
        @RequestPart("file", required = false) file: MultipartFile?,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseDTO<*> {
        logger.info { "프로필 변경 요청 controller에 도착" }
        val memberId: String = jwt.getClaimAsString("memberId")
        logger.info { "controller에 도착한 memberId: $memberId" }
        val requestDTO = memberConverter.profileRequestVOToDTO(requestVO, file)
        commandMemberService.patchProfile(memberId, requestDTO, file)
        return ResponseDTO.ok("프로필 변경 성공")
    }
}