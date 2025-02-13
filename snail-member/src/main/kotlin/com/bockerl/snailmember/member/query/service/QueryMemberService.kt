/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.member.query.service

import com.bockerl.snailmember.member.command.application.dto.MemberDTO

interface QueryMemberService {
    fun selectMemberByMemberId(memberId: Long): MemberDTO
}
