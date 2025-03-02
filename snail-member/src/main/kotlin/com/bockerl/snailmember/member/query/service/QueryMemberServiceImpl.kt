/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.member.query.service

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.command.application.dto.MemberDTO
import com.bockerl.snailmember.member.command.application.mapper.MemberConverter
import com.bockerl.snailmember.member.command.domain.aggregate.entity.MemberStatus
import com.bockerl.snailmember.member.query.repository.MemberMapper
import com.bockerl.snailmember.security.CustomMember
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service

@Service
class QueryMemberServiceImpl(private val memberMapper: MemberMapper, private val memberConverter: MemberConverter) :
    QueryMemberService {
    private val logger = KotlinLogging.logger {}

    override fun selectMemberByMemberId(memberId: String): MemberDTO {
        val member =
            memberMapper.selectMemberByMemberId(extractDigits(memberId))
                ?: throw CommonException(ErrorCode.NOT_FOUND_MEMBER)
        // Elvis 연산자로 왼쪽 값이 null일 경우 오른쪽 표현식 실행
        return memberConverter.entityToDTO(member)
    }

    override fun loadUserByUsername(username: String?): UserDetails {
        val email = username ?: throw BadCredentialsException("이메일이 제공되지 않았습니다")
        val member =
            memberMapper.selectMemberByMemberEmail(email)
                ?: throw BadCredentialsException("사용자를 찾을 수 없습니다: $email")

        if (member.memberStatus == MemberStatus.ROLE_BLACKLIST) {
            logger.info { "블랙리스트 멤버 로그인, email: ${member.memberEmail}" }
            throw LockedException("이 계정은 현재 사용이 제한되어 있습니다.")
        }

        val role = listOf(SimpleGrantedAuthority(member.memberStatus.toString()))

        return CustomMember(member, role)
    }

    private fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()
}
