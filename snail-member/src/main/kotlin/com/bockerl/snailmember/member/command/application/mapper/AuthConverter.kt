package com.bockerl.snailmember.member.command.application.mapper

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.command.application.dto.request.*
import com.bockerl.snailmember.member.command.domain.aggregate.vo.request.*
import org.springframework.stereotype.Component

@Component
class AuthConverter {
    // 회원가입 요청 vo to dto
    fun emailRequestVOToDTO(requestVO: EmailRequestVO) =
        EmailRequestDTO(
            memberEmail = requestVO.memberEmail ?: throw CommonException(ErrorCode.INVALID_INPUT_VALUE),
            memberNickName = requestVO.memberNickName ?: throw CommonException(ErrorCode.INVALID_INPUT_VALUE),
            memberBirth = requestVO.memberBirth ?: throw CommonException(ErrorCode.INVALID_INPUT_VALUE),
        )

    // 이메일 인증 코드 vo to dto
    fun emailVerifyRequestVOToDTO(requestVO: EmailVerifyRequestVO) =
        EmailVerifyRequestDTO(
            verificationCode = requestVO.verificationCode ?: throw CommonException(ErrorCode.INVALID_INPUT_VALUE),
            redisId = requestVO.redisId ?: throw CommonException(ErrorCode.INVALID_INPUT_VALUE),
        )

    // 휴대폰 인증 코드 요청 vo to dto
    fun phoneRequestVOToDTO(requestVO: PhoneRequestVO): PhoneRequestDTO =
        PhoneRequestDTO(
            phoneNumber = requestVO.phoneNumber ?: throw CommonException(ErrorCode.INVALID_INPUT_VALUE),
            redisId = requestVO.redisId ?: throw CommonException(ErrorCode.INVALID_INPUT_VALUE),
        )

    // 휴대폰 인증 코드 vo to dto
    fun phoneVerifyRequestVOToDTO(requestVO: PhoneVerifyRequestVO) =
        PhoneVerifyRequestDTO(
            verificationCode = requestVO.verificationCode ?: throw CommonException(ErrorCode.INVALID_INPUT_VALUE),
            redisId = requestVO.redisId ?: throw CommonException(ErrorCode.INVALID_INPUT_VALUE),
        )

    // 비밀번호 vo to dto
    fun passwordRequestVOToDTO(requestVO: PasswordRequestVO): PasswordRequestDTO =
        PasswordRequestDTO(
            password = requestVO.password ?: throw CommonException(ErrorCode.INVALID_INPUT_VALUE),
            redisId = requestVO.redisId ?: throw CommonException(ErrorCode.INVALID_INPUT_VALUE),
        )
}