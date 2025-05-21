package com.bockerl.snailmember.member.command.domain.service

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.command.application.service.MemberAuthService
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.VerificationType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.time.Duration
import kotlin.random.Random

@Service
class MemberAuthServiceImpl(
    private val redisTemplate: RedisTemplate<String, String>,
    private val mailSender: JavaMailSender,
) : MemberAuthService {
    private val logger = KotlinLogging.logger {}

    // 상수로 관리할 static 변수들
    companion object {
        private const val VERIFICATION_CODE_TTL = 5L
        private const val EMAIL_PREFIX = "verification:email:"
        private const val PHONE_PREFIX = "verification:phone:"
    }

    // 이메일 인증 코드 생성 메서드
    override fun createEmailVerificationCode(email: String) {
        logger.info { "새로운 이메일 인증 코드 생성 메서드 시작" }
        val verificationCode = generateCode()
        logger.info { "이메일 인증 코드 생성 성공: $verificationCode" }
        runCatching {
            saveVerificationCode(email, verificationCode, VerificationType.EMAIL)
            logger.info { "이메일 인증 코드 redis에 저장 성공" }
            sendVerificationEmail(email, verificationCode)
            logger.info { "이메일 인증 코드 이메일($email) 전송 성공" }
        }.onFailure { exception ->
            logger.error { "이메일 인증 코드 생성 중 에러 발생, 문제 이메일: $email" }
            logger.error { "ex: $exception" }
            redisTemplate.delete("$EMAIL_PREFIX$email")
            when (exception) {
                is MailException -> throw CommonException(ErrorCode.MAIL_SEND_FAIL)
                else -> throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
            }
        }
    }

    // 인증 메일을 보내는 메서드
    fun sendVerificationEmail(
        email: String,
        code: String,
    ) {
        logger.info { "인증 코드 메일 전송 메서드 시작($email): $code" }
        val message =
            SimpleMailMessage().apply {
                from = "bockerlsnail@gmail.com"
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

    // 휴대폰 인증 코드 생성 메서드
    override fun createPhoneVerificationCode(phoneNumber: String): String {
        logger.info { "새로운 휴대폰 인증 코드 생성 메서드 시작" }
        val verificationCode = generateCode()
        logger.info { "휴대폰 인증 코드 생성 성공: $verificationCode" }
        runCatching {
            saveVerificationCode(phoneNumber, verificationCode, VerificationType.PHONE)
            logger.info { "휴대폰 인증 코드 redis에 저장 성공" }
        }.onFailure {
            logger.error { "휴대폰 인증 코드 생성 중 에러 발생, 문제 번호: $phoneNumber" }
            redisTemplate.delete("$PHONE_PREFIX$phoneNumber")
            throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
        return verificationCode
    }

    // redis에 TTL(5분)으로 코드를 저장하는 공통 메서드
    fun saveVerificationCode(
        thing: String,
        code: String,
        type: VerificationType,
    ) {
        val key =
            when (type) {
                VerificationType.EMAIL -> "$EMAIL_PREFIX$thing"
                VerificationType.PHONE -> "$PHONE_PREFIX$thing"
            }
        // 미리 있을 수도 있는 value 삭제
        try {
            redisTemplate.delete(key)
            redisTemplate.opsForValue().set(key, code)
            redisTemplate.expire(key, Duration.ofMinutes(VERIFICATION_CODE_TTL))
        } catch (e: Exception) {
            logger.error { "redis 저장 중 에러 발생, e: $e" }
            throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
    }

    // 공통 인증 메서드
    override fun verifyCode(
        thing: String,
        verificationCode: String,
        type: VerificationType,
    ) {
        logger.info { "$type 인증 메서드 시작 - $type:$thing, code:$verificationCode" }
        val key =
            when (type) {
                VerificationType.EMAIL -> "$EMAIL_PREFIX$thing"
                VerificationType.PHONE -> "$PHONE_PREFIX$thing"
            }
        val savedCode =
            redisTemplate.opsForValue().get(key)
                ?: throw CommonException(ErrorCode.EXPIRED_CODE)
        logger.info { "redis에 저장되어 있던 코드: $savedCode" }
        // 코드 일치하지 않으면 예외 던지기
        if (savedCode != verificationCode) throw CommonException(ErrorCode.INVALID_CODE)
        logger.info { "redis 코드와 사용자 제공 코드가 일치함" }
        deleteExVerificationCode(thing, type)
    }

    // 무작위 숫자 5자리를 생성하는 공통 메서드
    private fun generateCode(): String = Random.nextInt(10000, 99999).toString()

    // redis에 이미 존재하는 코드 삭제하는 공통 메서드
    fun deleteExVerificationCode(
        redisId: String,
        type: VerificationType,
    ) {
        val key =
            when (type) {
                VerificationType.EMAIL -> "$EMAIL_PREFIX$redisId"
                VerificationType.PHONE -> "$PHONE_PREFIX$redisId"
            }
        try {
            redisTemplate.delete(key)
        } catch (e: Exception) {
            logger.error { "redis 삭제 중 에러 발생, e:$e" }
            throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
        logger.info { "redis에서 $type 인증 성공한 코드 삭제 성공" }
    }
}