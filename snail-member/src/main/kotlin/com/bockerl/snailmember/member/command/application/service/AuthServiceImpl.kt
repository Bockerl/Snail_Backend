package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.command.domain.vo.request.EmailRequestVO
import com.bockerl.snailmember.member.command.domain.vo.request.EmailVerifyRequestVO
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.time.Duration
import kotlin.random.Random

@Service
class AuthServiceImpl(
    private val redisTemplate: RedisTemplate<String, String>,
    private val mailSender: JavaMailSender,
) : AuthService {
    private val logger = KotlinLogging.logger {}

    // 상수로 관리할 static 변수들
    companion object {
        private const val VERIFICATION_CODE_LENGTH = 5
        private const val VERIFICATION_CODE_TTL = 5L
        private const val EMAIL_PREFIX = "verification:email:"
    }

    // 이메일 인증 코드 생성 메서드
    override fun createEmailVerificationCode(emailRequestVO: EmailRequestVO) {
        // let 연산자로 null 검증
        emailRequestVO.memberEmail?.let { email ->
            logger.info { "입력받은 인증용 이메일 주소: $email" }
            // 1. 인증 번호 생성
            val verificationCode = generateCode()
            logger.info { "새로 생성된 인증 코드(이메일-$email): $verificationCode" }
            executeEmailVerification(email, verificationCode)
        } ?: throw CommonException(ErrorCode.MISSING_REQUIRED_FIELD) // email null에 대한 예외
    }

    // 이메일 인증 코드 재생성 메서드
    override fun createEmailRefreshCode(emailRequestVO: EmailRequestVO) {
        // let 연산자로 null 검증
        emailRequestVO.memberEmail?.let { email ->
            logger.info { "입력받은 인증용 이메일 주소: $email" }
            // 1. Redis에 이미 존재하는 코드 삭제
            deleteExVerificationCode(email)
            // 2. 코드 재생성
            val verificationCode = generateCode()
            logger.info { "새로 재생성된 인증 코드(이메일-$email): $verificationCode" }
            executeEmailVerification(email, verificationCode)
        } ?: throw CommonException(ErrorCode.MISSING_REQUIRED_FIELD)
    }

    // 이메일 인증 메서드
    override fun verifyEmailCode(emailVerifyRequestVO: EmailVerifyRequestVO) {
        // 이메일 null 체크
        val email =
            emailVerifyRequestVO.memberEmail
                ?: throw CommonException(ErrorCode.MISSING_REQUEST_PARAMETER)
        emailVerifyRequestVO.verificationCode?.let { code ->
            logger.info { "인증 요청 코드:$code" }
            val key = "$EMAIL_PREFIX$email"
            val verificationCode =
                redisTemplate.opsForValue().get(key) ?: throw CommonException(ErrorCode.EXPIRED_CODE)
            logger.info { "redis에서 조회된 인증 코드: $verificationCode" }
            if (verificationCode != code) {
                logger.error { "인증 요청 코드가 redis와 불일치 - redis: $verificationCode, 사용자:$code" }
                throw CommonException(ErrorCode.INVALID_CODE)
            }
            // 성공 시 코드 삭제
            redisTemplate.delete(key)
            logger.info { "인증 성공 - 이메일: $email" }
        } ?: throw CommonException(ErrorCode.MISSING_REQUIRED_FIELD)
    }

    // 이메일 인증 코드 생성 공통 로직
    private fun executeEmailVerification(
        email: String,
        verificationCode: String,
    ) {
        runCatching {
            saveVerificationCode(email, verificationCode)
            logger.info { "인증 코드 redis에 저장 성공" }
            sendVerificationEmail(email, verificationCode)
            logger.info { "인증 코드 이메일($email) 전송 성공" }
        }.onFailure { exception ->
            logger.error { "이메일 인증 과정 중 에러 발생, 문제 이메일: $email" }
            redisTemplate.delete("$EMAIL_PREFIX$email")
            when (exception) {
                is MailException -> throw CommonException(ErrorCode.MAIL_SEND_FAIL)
                else -> throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
            }
        }
    }

    // redis에 이미 존재하는 코드 삭제
    private fun deleteExVerificationCode(email: String) {
        val key = "$EMAIL_PREFIX$email"
        redisTemplate.delete(key)
    }

    // redis에 TTL(5분)으로 코드를 저장하는 메서드
    private fun saveVerificationCode(
        email: String,
        code: String,
    ) {
        val key = "$EMAIL_PREFIX$email"
        redisTemplate.opsForValue().set(key, code)
        redisTemplate.expire(key, Duration.ofMinutes(VERIFICATION_CODE_TTL))
    }

    // 무작위 숫자 5자리를 생성하는 메서드
    private fun generateCode(): String = Random.nextInt(10000, 99999).toString()

    // 인증 메일을 보내는 메서드
    private fun sendVerificationEmail(
        email: String,
        code: String,
    ) {
        logger.info { "인증 코드 메일 전송 메서드 시작($email): $code" }
        val message =
            SimpleMailMessage().apply {
                setTo(email)
                subject = "이메일 인증 번호가 도착했습니다"
                text =
                    """
                    안녕하세요!
                    회원가입을 위한 인증번호가 발급되었습니다.
                    
                    인증번호: $code
                    
                    인증번호는 5분간 유효합니다.
                    인증번호 입력창에 위 번호를 입력해주세요.
                    """.trimIndent()
            }
        mailSender.send(message)
    }
}