/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.member.query.repository

import com.bockerl.snailmember.member.command.domain.aggregate.entity.Member
import org.apache.ibatis.annotations.Mapper

@Mapper
interface MemberMapper {
    // null이 조회될 수 있으므로 ?를 붙였습니다
    fun findMemberByMemberId(memberId: Long): Member?
}