/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.member.command.application.mapper

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
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
            memberNickName = dto.memberNickname,
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
    fun activityAreaRequestVOToDTO(requestVO: ActivityAreaRequestVO): ActivityAreaRequestDTO {
        val memberId = requestVO.memberId
        val primaryId = requestVO.primaryId

        if (memberId.isNullOrBlank() || !memberId.startsWith("MEM")) {
            throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT)
        }

        if (primaryId.isNullOrBlank() ||
            !primaryId.startsWith("EMD") ||
            primaryId == requestVO.workplaceId
        ) {
            throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT)
        }
        return ActivityAreaRequestDTO(
            memberId = requestVO.memberId,
            primaryId = requestVO.primaryId,
            workplaceId = requestVO.workplaceId,
        )
    }
}