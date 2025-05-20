/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.member.query.service

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.infrastructure.aop.Logging
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.MemberStatus
import com.bockerl.snailmember.member.query.dto.MemberQueryDTO
import com.bockerl.snailmember.member.query.repository.MemberMapper
import com.bockerl.snailmember.member.query.vo.MemberProfileResponseVO
import com.bockerl.snailmember.security.CustomMember
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class QueryMemberServiceImpl(
    private val memberMapper: MemberMapper,
) : QueryMemberService {
    private val logger = KotlinLogging.logger {}

    @Transactional(readOnly = true)
    override fun selectMemberByMemberId(memberId: String): MemberQueryDTO {
        val memberDTO =
            memberMapper.selectMemberByMemberId(extractDigits(memberId))
                ?: throw CommonException(ErrorCode.NOT_FOUND_MEMBER)
        return memberDTO
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    @Logging
    override fun selectMemberProfileByMemberId(memberId: String): MemberProfileResponseVO {
        logger.info { "자기 프로필 조회 서비스 메서드 시작" }
        val memberDTO =
            memberMapper.selectMemberByMemberId(extractDigits(memberId))
                ?: throw CommonException(ErrorCode.NOT_FOUND_MEMBER)
        logger.info { "조회된 memberDTO: $memberDTO" }
        return MemberProfileResponseVO(
            memberEmail = memberDTO.memberEmail,
            memberNickname = memberDTO.memberNickname,
            memberPhoto = memberDTO.memberPhoto,
            selfIntroduction = memberDTO.selfIntroduction,
        )
    }

    private fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()
}