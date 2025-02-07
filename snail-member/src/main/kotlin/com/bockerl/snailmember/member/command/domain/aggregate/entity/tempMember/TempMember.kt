package com.bockerl.snailmember.member.command.domain.aggregate.entity.tempMember

import jakarta.persistence.Id
import org.springframework.data.redis.core.RedisHash
import java.io.Serializable
import java.time.LocalDate
import java.util.UUID

// 단계적 회원 가입을 위한 임시 회원 클래스입니다.
@RedisHash("temp:member:") // Redis 키 prefix 지정
data class TempMember(
    // Redis 키의 고유 식별자로 사용될 필드
    @Id
    val redisId: String = UUID.randomUUID().toString(), // 기본값으로 UUID 생성
    val nickName: String,
    val birth: LocalDate,
    val email: String,
    var emailVerificationCode: String? = null,
    var phoneNumber: String? = null,
    var phoneNumberVerificationCode: String? = null,
    var password: String? = null,
    var signUpStep: SignUpStep = SignUpStep.INITIAL,
) : Serializable {
    // Jackson을 위한 기본 생성자
    private constructor() : this(
        redisId = UUID.randomUUID().toString(),
        nickName = "",
        birth = LocalDate.now(),
        email = "",
    )

    companion object {
        // Redis 키 생성을 위한 상수
        private const val KEY_PREFIX = "temp:member:"

        fun initiate(
            email: String,
            nickName: String,
            birth: LocalDate,
        ): TempMember =
            TempMember(
                nickName = nickName,
                birth = birth,
                email = email,
            )

        // Redis 키 생성 메서드
        fun createRedisKey(redisId: String): String = "$KEY_PREFIX$redisId"
    }

    fun verifyEmail(): TempMember =
        copy(
            signUpStep = SignUpStep.EMAIL_VERIFIED,
        )

    fun verifyPhoneNumber(phoneNumber: String): TempMember =
        copy(
            signUpStep = SignUpStep.PHONE_VERIFIED,
            phoneNumber = phoneNumber,
        )

    fun verifyPassword(password: String): TempMember =
        copy(
            signUpStep = SignUpStep.PASSWORD_VERIFIED,
            password = password,
        )
}