/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.member.command.application.dto.request.ProfileRequestDTO
import org.springframework.web.multipart.MultipartFile

interface CommandMemberService {
    fun putLastAccessTime(
        email: String,
        ipAddress: String,
        userAgent: String,
        idempotencyKey: String,
    )

    fun patchProfile(
        memberId: String,
        requestDTO: ProfileRequestDTO,
        file: MultipartFile?,
        idempotencyKey: String,
    )

    fun deleteMember(
        memberId: String,
        idempotencyKey: String,
    )
}