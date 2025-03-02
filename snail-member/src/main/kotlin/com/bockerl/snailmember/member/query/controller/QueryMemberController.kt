/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.member.query.controller

import com.bockerl.snailmember.common.ResponseDTO
import com.bockerl.snailmember.member.command.application.dto.MemberDTO
import com.bockerl.snailmember.member.command.application.mapper.MemberConverter
import com.bockerl.snailmember.member.query.service.QueryMemberService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/member")
class QueryMemberController(
    @Value("\${server.port}") private val serverPort: Int,
    private val queryMemberService: QueryMemberService,
    private val memberConverter: MemberConverter,
) {

    @Operation(
        summary = "Health Check 메서드",
        description = "서버의 health check를 합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "health check 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        examples = [
                            ExampleObject(
                                name = "default",
                                summary = "기본 응답",
                                description = "서버가 정상적으로 동작할 경우 응답입니다.",
                                value = "snail-member-service is alive and port: 8123",
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    @GetMapping("/health")
    fun healthCheck() = "snail-member-service is alive and port: $serverPort"

    @Operation(
        summary = "멤버 PK로 멤버 조회",
        description = "멤버 PK로 멤버ResponseVO를 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "멤버 조회 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = MemberDTO::class),
                    ),
                ],
            ),
        ],
    )
    @GetMapping("/{memberId}")
    fun getMemberByMemberId(@PathVariable memberId: String): ResponseDTO<*> {
        val memberDTO: MemberDTO = queryMemberService.selectMemberByMemberId(memberId)
        return ResponseDTO.ok(memberConverter.dtoToResponseVO(memberDTO))
    }
}
