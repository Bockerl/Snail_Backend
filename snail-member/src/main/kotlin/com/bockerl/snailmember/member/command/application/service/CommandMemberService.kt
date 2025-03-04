/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.member.command.application.dto.request.ActivityAreaRequestDTO

interface CommandMemberService {
    fun putLastAccessTime(memberEmail: String)

    fun postActivityArea(requestDTO: ActivityAreaRequestDTO)
}