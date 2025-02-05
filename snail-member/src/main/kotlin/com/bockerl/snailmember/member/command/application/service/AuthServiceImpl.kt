package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.command.domain.vo.request.EmailRequestVO
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

    override fun createEmailVerificationCode(emailRequestVO: EmailRequestVO) {
        // let 연산자로 null 검증
        emailRequestVO.memberEmail?.let { email ->
            logger.info { "입력받은 인증용 이메일 주소: $email" }
            // 1. 인증 번호 생성
            val verificationCode = generateCode()
            logger.info { "새로 생성된 인증 코드(이메일-$email): $verificationCode" }
            runCatching {
                // 2. Redis에 TTL로 저장
                saveVerificationCode(email, verificationCode)
                logger.info { "인증 코드 redis에 저장 성공" }
                // 3. 메일 전송
                sendVerificationEmail(email, verificationCode)
                logger.info { "인증 코드 이메일($email) 전송 성공" }
            }.onFailure { exception ->
                // 중간과정 실패 시 redis에 저장된 코드를 삭제 및 예외 던지기
                logger.error { "이메일 인증 과정 중 에러 발생, 문제 이메일: $email" }
                redisTemplate.delete("$EMAIL_PREFIX$email")
                when (exception) {
                    // Mail 관련 Exception이면 전용 예외 던지기
                    is MailException -> throw CommonException(ErrorCode.MAIL_SEND_FAIL)
                    // 아니면 그냥 서버 관련 에러 던지기
                    else -> throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
                }
            }
        } ?: throw CommonException(ErrorCode.MISSING_REQUIRED_FIELD) // null에 대한 예외
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