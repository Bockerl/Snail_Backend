/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.member.command.application.controller

import com.bockerl.snailmember.common.ResponseDTO
import com.bockerl.snailmember.infrastructure.config.OpenApiBody
import com.bockerl.snailmember.member.command.application.mapper.MemberConverter
import com.bockerl.snailmember.member.command.application.service.CommandMemberService
import com.bockerl.snailmember.member.command.domain.vo.request.ProfileRequestVO
import com.bockerl.snailmember.security.config.CurrentMemberId
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Encoding
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestHeader
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
        summary = "프로필 수정",
        description = """
        계정의 프로필 정보를 수정합니다.
        프로필 이미지는 선택사항이며, 10MB 이하의 이미지 파일만 업로드 가능합니다.
    """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "프로필 수정 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ResponseDTO::class),
                    ),
                ],
            ),
        ],
    )
    @OpenApiBody(
        content = [
            Content(
                encoding = [
                    Encoding(
                        name = "profileRequestVO",
                        contentType = MediaType.APPLICATION_JSON_VALUE,
                    ),
                ],
            ),
        ],
    )
    @PatchMapping("/profile", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun patchProfile(
        @RequestHeader("idempotencyKey") idempotencyKey: String,
        @RequestPart("profileRequestVO") requestVO: ProfileRequestVO,
        @RequestPart("file", required = false) file: MultipartFile?,
        @Parameter(hidden = true) @CurrentMemberId memberId: String,
    ): ResponseDTO<*> {
        logger.info { "프로필 변경 요청 controller 도착" }
        logger.info { "controller에 도착한 memberId: $memberId" }
        val requestDTO = memberConverter.profileRequestVOToDTO(requestVO, file)
        commandMemberService.patchProfile(memberId, requestDTO, file, idempotencyKey)
        return ResponseDTO.ok("프로필 변경 성공")
    }

    @Operation(
        summary = "회원 탈퇴",
        description = """
        회원 탈퇴를 시작합니다.
    """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "회원 탈퇴 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ResponseDTO::class),
                    ),
                ],
            ),
        ],
    )
    @DeleteMapping("/delete")
    fun deleteMember(
        @RequestHeader("idempotencyKey") idempotencyKey: String,
        @Parameter(hidden = true) @CurrentMemberId memberId: String,
    ): ResponseDTO<*> {
        logger.info { "회원 탈퇴 요청 controller 도착" }
        logger.info { "controller에 도착한 memberId: $memberId" }
        commandMemberService.deleteMember(memberId, idempotencyKey)
        return ResponseDTO.ok("회원 탈퇴 성공")
    }
}