@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.member.command.application.mapper

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.command.application.dto.request.*
import com.bockerl.snailmember.member.command.application.dto.response.LoginResponseDTO
import com.bockerl.snailmember.member.command.domain.aggregate.vo.request.*
import com.bockerl.snailmember.member.command.domain.vo.request.*
import com.bockerl.snailmember.member.command.domain.vo.response.LoginResponseVO
import org.springframework.stereotype.Component

@Component
class AuthConverter {
    // 회원가입 요청 vo to dto
    fun emailRequestVOToDTO(requestVO: EmailRequestVO) =
        EmailRequestDTO(
            memberEmail = requestVO.memberEmail ?: throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT),
            memberNickName = requestVO.memberNickName ?: throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT),
            memberBirth = requestVO.memberBirth ?: throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT),
        )

    // 이메일 인증 코드 vo to dto
    fun emailVerifyRequestVOToDTO(requestVO: EmailVerifyRequestVO) =
        EmailVerifyRequestDTO(
            verificationCode = requestVO.verificationCode ?: throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT),
            redisId = requestVO.redisId ?: throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT),
        )

    // 휴대폰 인증 코드 요청 vo to dto
    fun phoneRequestVOToDTO(requestVO: PhoneRequestVO): PhoneRequestDTO =
        PhoneRequestDTO(
            phoneNumber = requestVO.phoneNumber ?: throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT),
            redisId = requestVO.redisId ?: throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT),
        )

    // 휴대폰 인증 코드 vo to dto
    fun phoneVerifyRequestVOToDTO(requestVO: PhoneVerifyRequestVO) =
        PhoneVerifyRequestDTO(
            verificationCode = requestVO.verificationCode ?: throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT),
            redisId = requestVO.redisId ?: throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT),
        )

    // 비밀번호 vo to dto
    fun passwordRequestVOToDTO(requestVO: PasswordRequestVO): PasswordRequestDTO =
        PasswordRequestDTO(
            password = requestVO.password ?: throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT),
            redisId = requestVO.redisId ?: throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT),
        )

    // 회원가입 활동지역 vo to dto
    fun activityAreaRegisterRequestVOToDTO(requestVO: ActivityAreaRegisterRequestVO): ActivityAreaRegisterRequestDTO {
        val primaryId = requestVO.primaryFormattedId
        if (primaryId.isNullOrBlank() || !primaryId.startsWith("EMD")) {
            throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT)
        }
        return ActivityAreaRegisterRequestDTO(
            redisId = requestVO.redisId ?: throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT),
            primaryFormattedId = primaryId,
            // 직장 장소는 입력을 안 할 수도 있으므로 nullable
            workplaceFormattedId = requestVO.workplaceFormattedId,
        )
    }

    // 로그인 토큰 dto to vo
    fun loginDTOToVO(responseDTO: LoginResponseDTO): LoginResponseVO =
        LoginResponseVO(
            accessToken = responseDTO.accessToken,
            refreshToken = responseDTO.refreshToken,
        )
}