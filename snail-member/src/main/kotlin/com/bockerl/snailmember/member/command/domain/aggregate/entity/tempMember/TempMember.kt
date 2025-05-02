package com.bockerl.snailmember.member.command.domain.aggregate.entity.tempMember

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Id
import org.springframework.data.redis.core.RedisHash
import java.io.Serializable
import java.sql.Timestamp
import java.util.UUID

// 단계적 회원 가입을 위한 임시 회원 클래스입니다.
@RedisHash("temp:member:") // Redis 키 prefix 지정
data class TempMember(
    // Redis 키의 고유 식별자로 사용될 필드
    @Id
    val redisId: String = UUID.randomUUID().toString(), // 기본값으로 UUID 생성
    val nickName: String,
    val birth: Timestamp,
    val email: String,
    var phoneNumber: String = "",
    var password: String = "",
    @JsonProperty("signUpStep")
    var signUpStep: SignUpStep = SignUpStep.INITIAL,
) : Serializable {
    // Jackson을 위한 기본 생성자
    @JsonCreator
    constructor() : this(
        redisId = UUID.randomUUID().toString(),
        nickName = "",
        birth = Timestamp(System.currentTimeMillis()),
        email = "",
    )

    companion object {
        // Redis 키 생성을 위한 상수
        private const val KEY_PREFIX = "temp:member:"

        fun initiate(
            email: String,
            nickName: String,
            birth: Timestamp,
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

    fun verifyPhoneNumber(): TempMember =
        copy(
            signUpStep = SignUpStep.PHONE_VERIFIED,
        )

    fun verifyPassword(password: String): TempMember =
        copy(
            password = password,
            signUpStep = SignUpStep.PASSWORD_VERIFIED,
        )
}