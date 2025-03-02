/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.member.query.service

import com.bockerl.snailmember.member.command.application.dto.MemberDTO
import org.springframework.security.core.userdetails.UserDetailsService

interface QueryMemberService : UserDetailsService {
    fun selectMemberByMemberId(memberId: String): MemberDTO
}
