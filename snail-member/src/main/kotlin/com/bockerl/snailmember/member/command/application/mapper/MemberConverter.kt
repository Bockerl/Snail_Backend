/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.member.command.application.mapper

import com.bockerl.snailmember.member.command.application.dto.MemberDTO
import com.bockerl.snailmember.member.command.domain.aggregate.entity.Member
import com.bockerl.snailmember.member.command.domain.aggregate.vo.response.MemberResponseVO
import org.springframework.stereotype.Component

@Component
class MemberConverter {
    // Entity to DTO 변환
    fun entityToDTO(entity: Member): MemberDTO = MemberDTO(
        memberId = entity.formattedId,
        memberEmail = entity.memberEmail,
        memberPassword = entity.memberPassword,
        memberNickName = entity.memberNickName,
        memberPhoto = entity.memberPhoto,
        memberLanguage = entity.memberLanguage,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
        memberStatus = entity.memberStatus,
        memberGender = entity.memberGender,
        memberRegion = entity.memberRegion,
        memberPhoneNumber = entity.memberPhoneNumber,
        memberBirth = entity.memberBirth,
        lastAccessTime = entity.lastAccessTime,
        selfIntroduction = entity.selfIntroduction,
    )

    fun dtoToResponseVO(dto: MemberDTO): MemberResponseVO = MemberResponseVO(
        memberId = dto.memberId,
        memberEmail = dto.memberEmail,
        memberPassword = dto.memberPassword,
        memberNickName = dto.memberNickName,
        memberPhoto = dto.memberPhoto,
        memberLanguage = dto.memberLanguage,
        createdAt = dto.createdAt,
        updatedAt = dto.updatedAt,
        memberStatus = dto.memberStatus,
        memberGender = dto.memberGender,
        memberRegion = dto.memberRegion,
        memberPhoneNumber = dto.memberPhoneNumber,
        memberBirth = dto.memberBirth,
        lastAccessTime = dto.lastAccessTime,
        selfIntroduction = dto.selfIntroduction,
    )
}
