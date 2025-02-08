package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.command.application.dto.request.EmailRequestDTO
import com.bockerl.snailmember.member.command.application.dto.request.EmailVerifyRequestDTO
import com.bockerl.snailmember.member.command.application.dto.request.PhoneRequestDTO
import com.bockerl.snailmember.member.command.application.dto.request.PhoneVerifyRequestDTO
import com.bockerl.snailmember.member.command.domain.aggregate.entity.tempMember.SignUpStep
import com.bockerl.snailmember.member.command.domain.aggregate.entity.tempMember.TempMember
import com.bockerl.snailmember.member.command.domain.repository.TempMemberRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class RegistrationServiceImpl(
    private val authService: AuthService,
    private val tempMemberRepository: TempMemberRepository,
) : RegistrationService {
    private val logger = KotlinLogging.logger {}

    // 1.회원가입 시작(닉네임, 이메일, 생년월일 입력 및 이메일 코드 생성)
    override fun initiateRegistration(requestDTO: EmailRequestDTO): String {
        logger.info { "임시회원 생성 시작" }
        // redis에 저장할 임시회원 생성
        val tempMember =
            TempMember.initiate(
                email = requestDTO.memberEmail,
                nickName = requestDTO.memberNickName,
                birth = requestDTO.memberBirth,
            )
        logger.info { "임시 회원 객체 생성: $tempMember" }
        val redisId = tempMemberRepository.save(tempMember)
        logger.info { "임시 회원 객체 redisKey 생성완료: $redisId" }
        // 이메일 인증 요청
        authService.createEmailVerificationCode(requestDTO.memberEmail)
        logger.info { "이메일 인증 코드 메일 전송 성공 in registrationService" }
        return redisId
    }

    // 1-1.이메일 인증 코드 재요청
    override fun createEmailRefreshCode(redisId: String) {
        logger.info { "이메일 인증 코드 재요청 시작" }
        val tempMember =
            tempMemberRepository.find(redisId)
                ?: throw CommonException(ErrorCode.EXPIRED_SIGNUP_SESSION)
        if (tempMember.signUpStep != SignUpStep.INITIAL) {
            logger.error { "이메일 인증 순서가 아닌 상태에서 인증 요청이 날라온 에러 발생 - redisId: $redisId" }
            throw CommonException(ErrorCode.UNAUTHORIZED_ACCESS)
        }
        logger.info { "redis에서 저장된 tempMember: $tempMember" }
        authService.createEmailVerificationCode(tempMember.email)
    }

    // 2.이메일 인증 요청
    override fun verifyEmailCode(requestDTO: EmailVerifyRequestDTO): String {
        val redisId = requestDTO.redisId
        logger.info { "이메일 인증 시작 - key: $redisId" }
        // redis에서 tempMember 조회
        val tempMember =
            tempMemberRepository.find(redisId)
                ?: throw CommonException(ErrorCode.EXPIRED_SIGNUP_SESSION)
        logger.info { "redis에서 조회된 tempMember: $tempMember" }
        // 이메일 인증 순서인지 확인
        if (tempMember.signUpStep != SignUpStep.INITIAL) {
            logger.error { "이메일 인증 순서가 아닌 상태에서 인증 요청이 날라온 에러 발생 - redisId: $redisId" }
            throw CommonException(ErrorCode.UNAUTHORIZED_ACCESS)
        }
        // 이메일 인증 시도
        authService.verifyEmailCode(tempMember.email, requestDTO.verificationCode)
        // 이메일 인증 상태로 변경
        val updatedTempMember = tempMember.verifyEmail()
        logger.info { "이메일 인증 성공 후 redis에 업데이트되는 임시 회원: $updatedTempMember" }
        // 이메일 인증 성공 상태로 임시회원 상태 업데이트
        tempMemberRepository.update(redisId, updatedTempMember)
        logger.info { "redis에 임시회원 업데이트 성공" }
        return redisId
    }

    // 3. 휴대폰 인증 코드 생성
    override fun createPhoneVerificationCode(requestDTO: PhoneRequestDTO): String {
        val redisId = requestDTO.redisId
        logger.info { "휴대폰 인증 시작 - key: $redisId" }
        // redis에서 tempMember조회
        val tempMember =
            tempMemberRepository.find(redisId)
                ?: throw CommonException(ErrorCode.EXPIRED_SIGNUP_SESSION)
        logger.info { "redis에서 조회된 tempMember: $tempMember" }
        // 휴대폰 인증 순서인지 확인
        if (tempMember.signUpStep != SignUpStep.EMAIL_VERIFIED) {
            logger.error { "휴대폰 인증 순서가 아닌 상태에서 인증 요청이 날라온 에러 발생 - redisId: $redisId" }
            throw CommonException(ErrorCode.UNAUTHORIZED_ACCESS)
        }
        // 휴대폰 인증 코드 생성
        val verificationCode = authService.createPhoneVerificationCode(requestDTO.phoneNumber)
        // 임시 회원의 번호에 휴대폰 번호 등록
        tempMember.phoneNumber = requestDTO.phoneNumber
        // 임시 회원 상태 변경
        tempMemberRepository.update(redisId, tempMember)
        logger.info { "휴대폰 인증 코드 발송 성공" }
        return verificationCode
    }

    // 3-1. 휴대폰 인증 코드 재요청

    // 4. 휴대폰 인증 요청
    override fun verifyPhoneCode(requestDTO: PhoneVerifyRequestDTO): String {
        val redisId = requestDTO.redisId
        logger.info { "이메일 인증 시작 - key: $redisId" }
        // redis에서 tempMember 조회
        val tempMember =
            tempMemberRepository.find(redisId)
                ?: throw CommonException(ErrorCode.EXPIRED_SIGNUP_SESSION)
        logger.info { "redis에서 조회된 tempMember: $tempMember" }
        // 휴대폰 인증 순서인지 확인
        if (tempMember.signUpStep != SignUpStep.EMAIL_VERIFIED) {
            logger.error { "휴대폰 인증 순서가 아닌 상태에서 인증 요청이 날라온 에러 발생 - redisId: $redisId" }
            throw CommonException(ErrorCode.UNAUTHORIZED_ACCESS)
        }
        val phoneNumber = tempMember.phoneNumber
        // 휴대폰 인증 시도
        authService.verifyPhoneCode(phoneNumber, requestDTO.verificationCode)
        // 인증된 상태로 임시회원 변경
        val updatedMember = tempMember.verifyPhoneNumber()
        // 임시 회원 저장
        tempMemberRepository.update(redisId, updatedMember)
        return redisId
    }
}