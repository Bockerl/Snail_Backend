/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.member.command.application.mapper

import com.bockerl.snailmember.member.command.application.dto.request.ActivityAreaRequestDTO
import com.bockerl.snailmember.member.command.domain.aggregate.vo.request.ActivityAreaRequestVO
import com.bockerl.snailmember.member.command.domain.aggregate.vo.response.MemberResponseVO
import com.bockerl.snailmember.member.query.dto.MemberQueryDTO
import org.springframework.stereotype.Component

@Component
class MemberConverter {
    fun dtoToResponseVO(dto: MemberQueryDTO): MemberResponseVO =
        MemberResponseVO(
            memberId = dto.formattedId,
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

    // 활동지역 변경 혹은 oauth 회원을 위한 vo to dto
    fun activityAreaRequestVOToDTO(requestVO: ActivityAreaRequestVO): ActivityAreaRequestDTO =
        ActivityAreaRequestDTO(
            memberId = requestVO.validMemberId,
            primaryId = requestVO.validPrimaryId,
            workplaceId = requestVO.workplaceId,
        )
}