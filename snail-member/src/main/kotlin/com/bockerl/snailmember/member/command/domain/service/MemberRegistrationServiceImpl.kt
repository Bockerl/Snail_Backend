@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.member.command.domain.service

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.infrastructure.aop.Logging
import com.bockerl.snailmember.infrastructure.outbox.dto.OutboxDTO
import com.bockerl.snailmember.infrastructure.outbox.enums.EventType
import com.bockerl.snailmember.infrastructure.outbox.service.OutboxService
import com.bockerl.snailmember.member.command.application.dto.request.*
import com.bockerl.snailmember.member.command.application.service.MemberAuthService
import com.bockerl.snailmember.member.command.application.service.RegistrationService
import com.bockerl.snailmember.member.command.domain.aggregate.entity.*
import com.bockerl.snailmember.member.command.domain.aggregate.entity.TempMember
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.Gender
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.Language
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.MemberStatus
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.SignUpPath
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.SignUpStep
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.VerificationType
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberCreateEvent
import com.bockerl.snailmember.member.command.domain.repository.MemberRepository
import com.bockerl.snailmember.member.command.domain.repository.TempMemberRepository
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class MemberRegistrationServiceImpl(
    private val memberAuthService: MemberAuthService,
    private val tempMemberRepository: TempMemberRepository,
    private val memberRepository: MemberRepository,
    private val bcryptPasswordEncoder: BCryptPasswordEncoder,
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    private val eventPublisher: ApplicationEventPublisher,
    private val outboxService: OutboxService,
) : RegistrationService {
    private val logger = KotlinLogging.logger {}

    // 1.회원가입 시작(닉네임, 이메일, 생년월일 입력 및 이메일 코드 생성)
    @Transactional
    @Logging
    override fun initiateRegistration(
        requestDTO: EmailRequestDTO,
        idempotencyKey: String,
    ): String {
        logger.info { "임시회원 생성 시작" }
        // 멱등한 요청인지 확인
        redisTemplate.opsForValue().get(idempotencyKey)?.let { cacheResult ->
            throw CommonException(ErrorCode.INVALID_IDEMPOTENCY, "이미 처리된 임시회원 생성 요청입니다.")
        }
        // 넘어온 이메일이 이미 존재하는지 확인
        memberRepository
            .findMemberByMemberEmailAndMemberStatusNot(requestDTO.memberEmail, MemberStatus.ROLE_DELETED)
            ?.let { existingMember ->
                logger.warn { "이미 존재하는 회원 이메일로 회원가입 시도, email: $requestDTO.email" }
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
        memberAuthService.createEmailVerificationCode(requestDTO.memberEmail)
        // 멱등성을 위해
        redisTemplate.opsForValue().set(idempotencyKey, UUID.randomUUID().toString())
        return redisId
    }

    // 1-1.이메일 인증 코드 재요청
    @Transactional
    @Logging
    override fun createEmailRefreshCode(
        redisId: String,
        idempotencyKey: String,
    ) {
        logger.info { "이메일 인증 코드 재요청 시작" }
        // 멱등한 요청인지 확인
        redisTemplate.opsForValue().get(idempotencyKey)?.let { cacheResult ->
            throw CommonException(ErrorCode.INVALID_IDEMPOTENCY, "이미 처리된 이메일 코드 재요청 요청입니다.")
        }
        val tempMember =
            tempMemberRepository.find(redisId)
                ?: throw CommonException(ErrorCode.EXPIRED_SIGNUP_SESSION)
        logger.info { "redis에서 조회된 tempMember: $tempMember" }
        // 회원가입 단계 유효성 검사
        if (tempMember.signUpStep != SignUpStep.INITIAL) {
            logger.error { "이메일 인증 순서가 아닌 상태에서 인증 요청이 날라온 에러 발생 - redisId: $redisId" }
            throw CommonException(ErrorCode.UNAUTHORIZED_ACCESS)
        }
        memberAuthService.createEmailVerificationCode(tempMember.email)
        // 멱등성을 위해
        redisTemplate.opsForValue().set(idempotencyKey, UUID.randomUUID().toString())
    }

    // 2.이메일 인증 요청
    @Transactional
    @Logging
    override fun verifyEmailCode(
        requestDTO: EmailVerifyRequestDTO,
        idempotencyKey: String,
    ): String {
        // 멱등한 요청인지 확인
        redisTemplate.opsForValue().get(idempotencyKey)?.let { cacheResult ->
            throw CommonException(ErrorCode.INVALID_IDEMPOTENCY, "이미 처리된 이메일 인증 요청입니다.")
        }
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
        memberAuthService.verifyCode(tempMember.email, requestDTO.verificationCode, VerificationType.EMAIL)
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
            }.getOrThrow()
        // 멱등성을 위해
        redisTemplate.opsForValue().set(idempotencyKey, UUID.randomUUID().toString())
        return redisId
    }

    // 3. 휴대폰 인증 코드 생성
    @Transactional
    @Logging
    override fun createPhoneVerificationCode(
        requestDTO: PhoneRequestDTO,
        idempotencyKey: String,
    ): String {
        // 멱등한 요청인지 확인
        redisTemplate.opsForValue().get(idempotencyKey)?.let { cacheResult ->
            throw CommonException(ErrorCode.INVALID_IDEMPOTENCY, "이미 처리된 휴대폰 인증 코드 생성 요청입니다.")
        }
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
        val verificationCode = memberAuthService.createPhoneVerificationCode(requestDTO.phoneNumber)
        // 임시 회원의 번호에 휴대폰 번호 등록
        tempMember.phoneNumber = requestDTO.phoneNumber
        tempMemberRepository
            .runCatching {
                update(redisId, tempMember)
            }.onSuccess {
                logger.info { "redis에 임시회원 휴대폰 번호 업데이트 성공 - redisId: $redisId" }
            }.onFailure {
                logger.warn { "redis에 tempMember 저장 중 오류 발생, redisId: $redisId, error: $it" }
            }.getOrThrow()
        logger.info { "휴대폰 인증 코드 발송 성공" }
        // 멱등성을 위해
        redisTemplate.opsForValue().set(idempotencyKey, UUID.randomUUID().toString())
        return verificationCode
    }

    // 3-1. 휴대폰 인증 코드 재요청
    @Transactional
    @Logging
    override fun createPhoneRefreshCode(
        requestDTO: PhoneRequestDTO,
        idempotencyKey: String,
    ): String {
        logger.info { "휴대폰 인증 코드 재요청 시작" }
        redisTemplate.opsForValue().get(idempotencyKey)?.let { cacheResult ->
            throw CommonException(ErrorCode.INVALID_IDEMPOTENCY, "이미 처리된 휴대폰 인증 코드 재요청 요청입니다.")
        }
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
        val code = memberAuthService.createPhoneVerificationCode(tempMember.phoneNumber)
        logger.info { "새로 재생성된 핸드폰 인증 코드: $code" }
        // 멱등성을 위해
        redisTemplate.opsForValue().set(idempotencyKey, UUID.randomUUID().toString())
        return code
    }

    // 4. 휴대폰 인증 요청
    @Transactional
    @Logging
    override fun verifyPhoneCode(
        requestDTO: PhoneVerifyRequestDTO,
        idempotencyKey: String,
    ): String {
        // 멱등한 요청인지 확인
        redisTemplate.opsForValue().get(idempotencyKey)?.let { cacheResult ->
            throw CommonException(ErrorCode.INVALID_IDEMPOTENCY, "이미 처리된 휴대폰 인증 요청입니다.")
        }
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
        memberAuthService.verifyCode(phoneNumber, requestDTO.verificationCode, VerificationType.PHONE)
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
            }.getOrThrow()
        // 멱등성을 위해
        redisTemplate.opsForValue().set(idempotencyKey, UUID.randomUUID().toString())
        return redisId
    }

    // 5. 비밀번호 입력(회원가입 완료)
    @Transactional
    @Logging
    override fun postPassword(
        requestDTO: PasswordRequestDTO,
        idempotencyKey: String,
    ) {
        // 멱등한 요청인지 확인
        redisTemplate.opsForValue().get(idempotencyKey)?.let { cacheResult ->
            throw CommonException(ErrorCode.INVALID_IDEMPOTENCY, "이미 처리된 비밀번호 입력 요청입니다.")
        }
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
        // 실제 회원 생성 후 등록
        val newMember =
            Member(
                memberEmail = tempMember.email,
                memberNickname = tempMember.nickName,
                // tempMember의 timestamp를 localDate로 변환
                memberBirth = tempMember.birth.toLocalDateTime().toLocalDate(),
                // 비밀번호 암호화
                memberPassword = bcryptPasswordEncoder.encode(requestDTO.password),
                memberPhoneNumber = tempMember.phoneNumber,
                memberPhoto = "",
                memberGender = Gender.UNKNOWN,
                memberLanguage = Language.KOR,
                memberRegion = "",
                memberStatus = MemberStatus.ROLE_TEMP,
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
            }.getOrThrow()
        tempMemberRepository
            .runCatching {
                delete(redisId)
            }.onSuccess {
                logger.info { "redis에 저장된 임시 회원 삭제 성공 - redisId: $redisId" }
            }.onFailure {
                logger.info { "redis에 저장된 임시 회원 삭제 실패 - redisId: $redisId" }
                throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
            }.getOrThrow()
        logger.info { "회원 가입 종료 - 회원 가입 성공" }
        // 멱등성을 위해
        redisTemplate.opsForValue().set(idempotencyKey, UUID.randomUUID().toString())
        // 회원 생성 이벤트
        val memberEvent =
            MemberCreateEvent(
                memberId = newMember.formattedId,
                memberEmail = newMember.memberEmail,
                memberPhoneNumber = newMember.memberPhoneNumber,
                memberNickname = newMember.memberNickname,
                memberGender = newMember.memberGender,
                memberLanguage = newMember.memberLanguage,
                memberRegion = newMember.memberRegion,
                memberPhoto = newMember.memberPhoto,
                memberBirth = newMember.memberBirth,
                memberStatus = newMember.memberStatus,
                signUpPath = SignUpPath.EMAIL,
            )
        // logging을 위한 비동기 리스너 이벤트 처리
        eventPublisher.publishEvent(memberEvent)
        // outbox 이벤트 처리
        val jsonPayLoad = objectMapper.writeValueAsString(memberEvent)
        val outBox =
            OutboxDTO(
                newMember.formattedId,
                EventType.MEMBER,
                jsonPayLoad,
                idempotencyKey,
            )
        outboxService.createOutbox(outBox)
    }
}