@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.member.command.domain.service

import com.bockerl.snailmember.area.command.domain.aggregate.entity.ActivityArea
import com.bockerl.snailmember.area.command.domain.aggregate.entity.AreaType
import com.bockerl.snailmember.area.command.domain.repository.ActivityAreaRepository
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.command.application.dto.request.*
import com.bockerl.snailmember.member.command.application.service.AuthService
import com.bockerl.snailmember.member.command.application.service.RegistrationService
import com.bockerl.snailmember.member.command.domain.aggregate.entity.*
import com.bockerl.snailmember.member.command.domain.aggregate.entity.tempMember.SignUpStep
import com.bockerl.snailmember.member.command.domain.aggregate.entity.tempMember.TempMember
import com.bockerl.snailmember.member.command.domain.aggregate.entity.tempMember.VerificationType
import com.bockerl.snailmember.member.command.domain.repository.MemberRepository
import com.bockerl.snailmember.member.command.domain.repository.TempMemberRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class RegistrationServiceImpl(
    private val authService: AuthService,
    private val tempMemberRepository: TempMemberRepository,
    private val memberRepository: MemberRepository,
    private val activityAreaRepository: ActivityAreaRepository,
    private val bcryptPasswordEncoder: BCryptPasswordEncoder,
) : RegistrationService {
    private val logger = KotlinLogging.logger {}

    // 1.회원가입 시작(닉네임, 이메일, 생년월일 입력 및 이메일 코드 생성)
    @Transactional
    override fun initiateRegistration(requestDTO: EmailRequestDTO): String {
        logger.info { "임시회원 생성 시작" }
        // 넘어온 이메일이 이미 존재하는지 확인
        memberRepository.findMemberByMemberEmail(requestDTO.memberEmail)?.let { existingMember ->
            logger.warn { "이미 존재하는 이메일로 회원가입 시도, email: $requestDTO.email" }
            throw CommonException(ErrorCode.EXIST_USER)
        }
        // redis에 저장할 임시회원 생성
        val tempMember =
            TempMember.initiate(
                email = requestDTO.memberEmail,
                nickName = requestDTO.memberNickName,
                birth = requestDTO.memberBirth,
            )
        logger.info { "임시 생성된 회원 객체 정보: $tempMember" }
        val redisId = tempMemberRepository.save(tempMember)
        logger.info { "임시 회원 객체 redis에 저장 성공 redisId: $redisId" }
        // 이메일 인증 요청
        authService.createEmailVerificationCode(requestDTO.memberEmail)
        return redisId
    }

    // 1-1.이메일 인증 코드 재요청
    @Transactional
    override fun createEmailRefreshCode(redisId: String) {
        logger.info { "이메일 인증 코드 재요청 시작" }
        val tempMember =
            tempMemberRepository.find(redisId)
                ?: throw CommonException(ErrorCode.EXPIRED_SIGNUP_SESSION)
        logger.info { "redis에서 조회된 tempMember: $tempMember" }
        // 회원가입 단계 유효성 검사
        if (tempMember.signUpStep != SignUpStep.INITIAL) {
            logger.error { "이메일 인증 순서가 아닌 상태에서 인증 요청이 날라온 에러 발생 - redisId: $redisId" }
            throw CommonException(ErrorCode.UNAUTHORIZED_ACCESS)
        }
        authService.createEmailVerificationCode(tempMember.email)
    }

    // 2.이메일 인증 요청
    @Transactional
    override fun verifyEmailCode(requestDTO: EmailVerifyRequestDTO): String {
        val redisId = requestDTO.redisId
        logger.info { "이메일 인증 시작 - redisId: $redisId" }
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
        authService.verifyCode(tempMember.email, requestDTO.verificationCode, VerificationType.EMAIL)
        // 이메일 인증 상태로 변경
        val updatedTempMember = tempMember.verifyEmail()
        logger.info { "이메일 인증 성공 후 tempMember: $tempMember" }
        // 이메일 인증 성공 상태로 임시회원 상태 업데이트
        tempMemberRepository
            .runCatching {
                update(redisId, updatedTempMember)
            }.onSuccess {
                logger.info { "redis에 임시회원 이메일 인증 업데이트 성공 - redisId: $redisId" }
            }.onFailure {
                logger.warn { "redis에 tempMember 저장 중 오류 발생, redisId: $redisId, error: $it" }
            }
        return redisId
    }

    // 3. 휴대폰 인증 코드 생성
    @Transactional
    override fun createPhoneVerificationCode(requestDTO: PhoneRequestDTO): String {
        val redisId = requestDTO.redisId
        logger.info { "휴대폰 인증 시작 - redisId: $redisId" }
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
        tempMemberRepository
            .runCatching {
                update(redisId, tempMember)
            }.onSuccess {
                logger.info { "redis에 임시회원 휴대폰 번호 업데이트 성공 - redisId: $redisId" }
            }.onFailure {
                logger.warn { "redis에 tempMember 저장 중 오류 발생, redisId: $redisId, error: $it" }
            }
        logger.info { "휴대폰 인증 코드 발송 성공" }
        return verificationCode
    }

    // 3-1. 휴대폰 인증 코드 재요청
    @Transactional
    override fun createPhoneRefreshCode(requestDTO: PhoneRequestDTO): String {
        logger.info { "휴대폰 인증 코드 재요청 시작" }
        val redisId = requestDTO.redisId
        logger.info { "휴대폰 인증 시작 - redisId: $redisId" }
        val tempMember =
            tempMemberRepository.find(redisId)
                ?: throw CommonException(ErrorCode.EXPIRED_SIGNUP_SESSION)
        logger.info { "redis에서 조회된 tempMember: $tempMember" }
        if (tempMember.signUpStep != SignUpStep.EMAIL_VERIFIED) {
            logger.error { "휴대폰 인증 순서가 아닌 상태에서 인증 요청이 날라온 에러 발생 - redisId: $redisId" }
            throw CommonException(ErrorCode.UNAUTHORIZED_ACCESS)
        }
        val code = authService.createPhoneVerificationCode(tempMember.phoneNumber)
        logger.info { "새로 재생성된 핸드폰 인증 코드: $code" }
        return code
    }

    // 4. 휴대폰 인증 요청
    @Transactional
    override fun verifyPhoneCode(requestDTO: PhoneVerifyRequestDTO): String {
        val redisId = requestDTO.redisId
        logger.info { "이메일 인증 시작 - redisId: $redisId" }
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
        authService.verifyCode(phoneNumber, requestDTO.verificationCode, VerificationType.PHONE)
        // 인증된 상태로 임시회원 변경
        val updatedMember = tempMember.verifyPhoneNumber()
        // 임시 회원 저장
        tempMemberRepository
            .runCatching {
                update(redisId, updatedMember)
            }.onSuccess {
                logger.info { "redis에 임시회원 휴대폰 번호 업데이트 성공 - redisId: $redisId" }
            }.onFailure {
                logger.info { "redis에 임시회원 휴대폰 번호 업데이트 실패 - redisId: $redisId" }
                throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
            }
        return redisId
    }

    // 5. 비밀번호 입력
    @Transactional
    override fun postPassword(requestDTO: PasswordRequestDTO): String {
        val redisId = requestDTO.redisId
        logger.info { "비밀번호 입력 시작 - redisId: $redisId" }
        // redis에서 tempMember 조회
        val tempMember =
            tempMemberRepository.find(redisId)
                ?: throw CommonException(ErrorCode.EXPIRED_SIGNUP_SESSION)
        logger.info { "redis에서 조회된 tempMember: $tempMember" }
        // 비밀번호 입력 순서인지 확인
        if (tempMember.signUpStep != SignUpStep.PHONE_VERIFIED) {
            logger.error { "비밀번호 입력 순서가 아닌 상태에서 인증 요청이 날라온 에러 발성 - redisId: $redisId" }
            throw CommonException(ErrorCode.UNAUTHORIZED_ACCESS)
        }
        // 비밀번호 입력한 임시회원 상태로 변경
        val updateMember = tempMember.verifyPassword(requestDTO.password)
        tempMemberRepository
            .runCatching {
                update(redisId, updateMember)
            }.onSuccess {
                logger.info { "redis에 임시회원 비밀번호 업데이트 성공 - redisId: $redisId" }
            }.onFailure {
                logger.info { "redis에 임시회원 비밀번호 업데이트 실패 - redisId: $redisId" }
                throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
            }
        return redisId
    }

    // 6. 활동지역 등록(회원가입 완료)
    @Transactional
    override fun postActivityArea(requestDTO: ActivityAreaRegisterRequestDTO) {
        val redisId = requestDTO.redisId
        logger.info { "활동지역 등록 시작 - redisId: $redisId" }
        // redis에서 tempMember 조회
        val tempMember =
            tempMemberRepository.find(redisId)
                ?: throw CommonException(ErrorCode.EXPIRED_SIGNUP_SESSION)
        logger.info { "redis에서 조회된 tempMember: $tempMember" }
        // 활동지역 등록 순서인지 확인
        if (tempMember.signUpStep != SignUpStep.PASSWORD_VERIFIED) {
            logger.error { "활동지역 등록 순서가 아닌 상태에서 등록 요청이 날라온 에러 발생 - redisId: $redisId" }
            throw CommonException(ErrorCode.UNAUTHORIZED_ACCESS)
        }
        // 실제 회원 생성 후 등록
        val newMember =
            Member(
                memberEmail = tempMember.email,
                memberNickname = tempMember.nickName,
                // tempMember의 timestamp를 localDate로 변환
                memberBirth = tempMember.birth.toLocalDateTime().toLocalDate(),
                // 비밀번호 암호화
                memberPassword = bcryptPasswordEncoder.encode(tempMember.password),
                memberPhoneNumber = tempMember.phoneNumber,
                memberPhoto = "",
                memberGender = Gender.UNKNOWN,
                memberLanguage = Language.KOR,
                memberRegion = "",
                memberStatus = MemberStatus.ROLE_USER,
                signupPath = SignUpPath.EMAIL,
                selfIntroduction = "",
            )
        memberRepository
            .runCatching {
                save(newMember)
            }.onSuccess {
                logger.info { "메인 DB에 새 회원 저장 성공 - newMember: $newMember" }
            }.onFailure {
                logger.warn { "메인 DB에 새 회원 저장 실패 - newMember: $newMember" }
                throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
            }
        val primaryId =
            extractDigits(requestDTO.primaryFormattedId)
                .also { logger.info { "추출된 primaryAreaId: $it" } }
        // 활동지역 db에 저장
        // 1. 주 지역은 반드시 존재하므로 그냥 저장
        val primaryArea =
            ActivityArea(
                id =
                    ActivityArea.ActivityId(
                        memberId = newMember.memberId ?: throw CommonException(ErrorCode.NOT_FOUND_USER_ID),
                        emdAreasId = primaryId,
                    ),
                areaType = AreaType.PRIMARY,
                createdAt = LocalDateTime.now(),
            ).also { logger.info { "새로 생성된 메인 활동 지역: $it" } }
        activityAreaRepository
            .runCatching {
                save(primaryArea)
            }.onSuccess {
                logger.info { "메인 DB에 새 메인 활동 지역 저장 성공 - primaryArea: $primaryArea" }
            }.onFailure {
                logger.warn { "메인 DB에 새 메인 활동 지역 저장 실패 - primaryArea: $primaryArea" }
                throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
            }
        // 2. 직장 근처는 let을 통해 저장
        requestDTO.workplaceFormattedId?.let { secondaryId ->
            logger.info { "직장 근처 활동 지역 저장 시작, secondaryId: $secondaryId" }
            if (secondaryId == requestDTO.primaryFormattedId) {
                logger.error { "주 활동 지역과 직장 근처 활동 지역이 같은 종류로 등록되는 에러 발생, secondaryId: $secondaryId" }
                throw CommonException(ErrorCode.UNAUTHORIZED_ACCESS)
            }
            val workplaceArea =
                ActivityArea(
                    id =
                        ActivityArea.ActivityId(
                            memberId = newMember.memberId ?: throw CommonException(ErrorCode.NOT_FOUND_USER_ID),
                            emdAreasId = extractDigits(secondaryId),
                        ),
                    areaType = AreaType.WORKPLACE,
                    createdAt = LocalDateTime.now(),
                ).also { logger.info { "새로 생성된 직장 근처 활동 지역: $it" } }
            activityAreaRepository
                .runCatching {
                    save(workplaceArea)
                }.onSuccess {
                    logger.info { "메인 DB에 새 직장 활동 지역 저장 성공 - workplaceArea: $workplaceArea" }
                }.onFailure {
                    logger.warn { "메인 DB에 새 직장 활동 지역 저장 실패 - workplaceArea: $workplaceArea" }
                    throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
                }
        }
        tempMemberRepository
            .runCatching {
                delete(redisId)
            }.onSuccess {
                logger.info { "redis에 저장된 임시 회원 삭제 성공 - redisId: $redisId" }
            }.onFailure {
                logger.info { "redis에 저장된 임시 회원 삭제 실패 - redisId: $redisId" }
                throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
            }
        logger.info { "회원 가입 종료 - 회원 가입 성공" }
    }

    private fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()
}