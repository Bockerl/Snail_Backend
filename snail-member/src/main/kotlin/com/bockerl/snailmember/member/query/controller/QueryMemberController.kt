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
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/member")
class QueryMemberController(
    private val queryMemberService: QueryMemberService,
    private val memberConverter: MemberConverter,
) {
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
    fun getMemberByMemberId(@PathVariable memberId: Long): ResponseDTO<*> {
        val memberDTO: MemberDTO = queryMemberService.selectMemberByMemberId(memberId)
        return ResponseDTO.ok(memberConverter.dtoToResponseVO(memberDTO))
    }
}
