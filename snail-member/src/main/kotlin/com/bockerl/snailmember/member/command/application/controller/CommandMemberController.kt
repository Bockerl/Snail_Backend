/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.member.command.application.controller

import com.bockerl.snailmember.common.ResponseDTO
import com.bockerl.snailmember.member.command.application.mapper.MemberConverter
import com.bockerl.snailmember.member.command.application.service.CommandMemberService
import com.bockerl.snailmember.member.command.domain.aggregate.vo.request.ActivityAreaRequestVO
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/member")
class CommandMemberController(
    private val commandMemberService: CommandMemberService,
    private val memberConverter: MemberConverter,
) {
    private val logger = KotlinLogging.logger {}

    @PostMapping("/activity_area")
    fun postActivityArea(
        @RequestBody requestVO: ActivityAreaRequestVO,
    ): ResponseDTO<*> {
        logger.info { "활동지역 설정 요청 controller에 도착" }
        requestVO.apply {
            validatePrimaryId()
        }
        // 유효성 검사
        requestVO.apply {
            validatePrimaryId()
        }
        val requestDTO = memberConverter.activityAreaRequestVOToDTO(requestVO)
        commandMemberService.postActivityArea(requestDTO)
        return ResponseDTO.ok("활동지역 설정 성공")
    }
}