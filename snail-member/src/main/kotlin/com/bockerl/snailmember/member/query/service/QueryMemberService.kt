package com.bockerl.snailmember.member.query.service

import com.bockerl.snailmember.member.command.application.dto.MemberDTO

interface QueryMemberService {
    fun findMemberByMemberId(memberId: Long): MemberDTO
}