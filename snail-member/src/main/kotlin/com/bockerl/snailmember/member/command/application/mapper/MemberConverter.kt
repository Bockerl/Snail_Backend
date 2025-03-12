/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.member.command.application.mapper

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.command.application.dto.request.ActivityAreaRequestDTO
import com.bockerl.snailmember.member.command.application.dto.request.ProfileRequestDTO
import com.bockerl.snailmember.member.command.domain.aggregate.entity.Gender
import com.bockerl.snailmember.member.command.domain.vo.request.ActivityAreaRequestVO
import com.bockerl.snailmember.member.command.domain.vo.request.ProfileRequestVO
import com.bockerl.snailmember.member.command.domain.vo.response.MemberResponseVO
import com.bockerl.snailmember.member.query.dto.MemberQueryDTO
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

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
        val primaryId = requestVO.primaryId

        if (primaryId.isNullOrBlank() ||
            !primaryId.startsWith("EMD") ||
            primaryId == requestVO.workplaceId
        ) {
            throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT)
        }
        return ActivityAreaRequestDTO(
            primaryId = requestVO.primaryId,
            workplaceId = requestVO.workplaceId,
        )
    }

    fun profileRequestVOToDTO(
        requestVO: ProfileRequestVO,
        file: MultipartFile?,
    ): ProfileRequestDTO {
        val nickName = requestVO.nickName
        val birth = requestVO.birth
        val gender = requestVO.gender
        val selfIntro = requestVO.selfIntroduction

        if (nickName.isNullOrBlank()) {
            throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT)
        }

        if (birth == null) {
            throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT)
        }

        val today = LocalDate.now()
        val minDate = today.minusYears(120)

        if (birth.isBefore(minDate)) {
            throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT)
        }

        if (gender == null || gender !in Gender.entries.toTypedArray()) {
            throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT)
        }

        if (selfIntro == null) {
            throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT)
        }

        return ProfileRequestDTO(
            nickName = nickName,
            birth = birth,
            selfIntroduction = selfIntro,
            gender = gender,
        )
    }
}