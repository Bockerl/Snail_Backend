/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.member.command.application.mapper

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.command.application.dto.request.ProfileRequestDTO
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.Gender
import com.bockerl.snailmember.member.command.domain.vo.request.ProfileRequestVO
import com.bockerl.snailmember.member.command.domain.vo.response.MemberResponseVO
import com.bockerl.snailmember.member.query.dto.MemberProfileResponseDTO
import com.bockerl.snailmember.member.query.dto.MemberQueryDTO
import com.bockerl.snailmember.member.query.vo.MemberProfileResponseVO
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

@Component
class MemberConverter {
    private val logger = KotlinLogging.logger {}

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

    fun profileRequestVOToDTO(
        requestVO: ProfileRequestVO,
        file: MultipartFile?,
    ): ProfileRequestDTO {
        val nickName = requestVO.nickName
        val birth = requestVO.birth
        val gender = requestVO.gender
        val selfIntro = requestVO.selfIntroduction

        if (nickName.isNullOrBlank()) {
            throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT, "유효하지 않은 닉네임입니다.")
        }

        if (birth == null) {
            throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT, "생일이 null입니다.")
        }

        val today = LocalDate.now()
        val minDate = today.minusYears(120)
        val maxDate = today.minusYears(14)

        if (birth.isBefore(minDate) || birth.isAfter(maxDate)) {
            throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT, "유효하지 않은 생년월일입니다.")
        }

        if (gender == null || gender !in Gender.entries.toTypedArray()) {
            throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT, "유효하지 않은 성별값입니다.")
        }

        if (selfIntro == null) {
            throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT, "자기소개가 null입니다.")
        }

        return ProfileRequestDTO(
            nickName = nickName,
            birth = birth,
            selfIntroduction = selfIntro,
            gender = gender,
        )
    }

    fun profileResponseVOToDTO(responseVO: MemberProfileResponseVO): MemberProfileResponseDTO {
        val email = responseVO.memberEmail
        val nickName = responseVO.memberNickname
        val photo = responseVO.memberPhoto
        val selfIntroduction = responseVO.selfIntroduction

        return MemberProfileResponseDTO(
            memberEmail = email,
            memberNickname = nickName,
            memberPhoto = photo,
            selfIntroduction = selfIntroduction,
        )
    }
}