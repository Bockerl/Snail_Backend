/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.member.query.service

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.command.application.dto.MemberDTO
import com.bockerl.snailmember.member.command.application.mapper.MemberConverter
import com.bockerl.snailmember.member.query.repository.MemberMapper
import org.springframework.stereotype.Service

@Service
class QueryMemberServiceImpl(
    private val memberMapper: MemberMapper,
    private val memberConverter: MemberConverter,
) : QueryMemberService {
    override fun selectMemberByMemberId(memberId: String): MemberDTO {
        val parsingMemberId = extractDigits(memberId)

        val member =
            memberMapper.selectMemberByMemberId(parsingMemberId)
                ?: throw CommonException(ErrorCode.NOT_FOUND_MEMBER)
        // Elvis 연산자로 왼쪽 값이 null일 경우 오른쪽 표현식 실행
        return memberConverter.entityToDTO(member)
    }

    fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()
}