package com.bockerl.snailmember.utils

import com.bockerl.snailmember.member.command.application.dto.request.ActivityAreaRequestDTO
import com.bockerl.snailmember.member.command.application.dto.request.EmailRequestDTO
import com.bockerl.snailmember.member.command.application.dto.request.EmailVerifyRequestDTO
import com.bockerl.snailmember.member.command.application.dto.request.PasswordRequestDTO
import com.bockerl.snailmember.member.command.application.dto.request.PhoneRequestDTO
import com.bockerl.snailmember.member.command.application.dto.request.PhoneVerifyRequestDTO
import com.bockerl.snailmember.member.command.application.dto.response.KaKaoTokenResponseDTO
import com.bockerl.snailmember.member.command.domain.aggregate.entity.tempMember.SignUpStep
import com.bockerl.snailmember.member.command.domain.aggregate.entity.tempMember.TempMember
import java.sql.Timestamp

const val TEST_EMAIL = "test@test.com"
const val TEST_NICKNAME = "testUser"
const val TEST_PHONE = "01012345678"
const val TEST_PASSWORD = "Password123!"
const val TEST_REDIS_ID = "test-redis-id"
const val VERIFICATION_CODE = "12345"
val TEST_BIRTH = Timestamp(1708300800000)
const val TEST_PRIMARY_AREA = "Emd-00000001"
const val TEST_WORKPLACE_AREA = "Emd-00000002"
const val EMAIL_PREFIX = "verification:email:"
const val PHONE_PREFIX = "verification:phone:"
const val VERIFICATION_TTL = 5L
const val TEST_CLIENT_ID = "test-client-id"
const val TEST_CLIENT_SECRET = "test-client-secret"
const val TEST_REDIRECT_URI = "test-redirect-uri"
const val TEST_CODE = "test-code"
const val TEST_TOKEN_TYPE = "test-token"
const val TEST_ACCESS_TOKEN = "test-access-token"
const val TEST_REFRESH_TOKEN = "test-refresh-token"
const val TEST_ID_TOKEN = "test-id-token"

fun createTempMember(
    redisId: String = TEST_REDIS_ID,
    nickname: String = TEST_NICKNAME,
    birth: Timestamp = TEST_BIRTH,
    phoneNumber: String = TEST_PHONE,
    password: String = TEST_PASSWORD,
    email: String = TEST_EMAIL,
    signUpStep: SignUpStep = SignUpStep.INITIAL,
): TempMember = TempMember(
    redisId = redisId,
    nickName = nickname,
    birth = birth,
    phoneNumber = phoneNumber,
    password = password,
    email = email,
    signUpStep = signUpStep,
)

fun createInitialTempMember(): TempMember = TempMember(
    email = TEST_EMAIL,
    nickName = TEST_NICKNAME,
    birth = TEST_BIRTH,
)

fun createEmailRequestDTO(
    email: String = TEST_EMAIL,
    nickname: String = TEST_NICKNAME,
    birth: Timestamp = TEST_BIRTH,
): EmailRequestDTO = EmailRequestDTO(
    memberEmail = email,
    memberNickName = nickname,
    memberBirth = birth,
)

fun createEmailVerifyRequestDTO(
    redisId: String = TEST_REDIS_ID,
    code: String = VERIFICATION_CODE,
): EmailVerifyRequestDTO = EmailVerifyRequestDTO(
    redisId = redisId,
    verificationCode = code,
)

fun createPhoneRequestDTO(redisId: String = TEST_REDIS_ID, phoneNumber: String = TEST_PHONE): PhoneRequestDTO =
    PhoneRequestDTO(
        redisId = redisId,
        phoneNumber = phoneNumber,
    )

fun createPhoneVerifyRequestDTO(
    redisId: String = TEST_REDIS_ID,
    code: String = VERIFICATION_CODE,
): PhoneVerifyRequestDTO = PhoneVerifyRequestDTO(
    redisId = redisId,
    verificationCode = code,
)

fun createPassWordRequestDTO(redisId: String = TEST_REDIS_ID, password: String = TEST_PASSWORD): PasswordRequestDTO =
    PasswordRequestDTO(
        redisId = redisId,
        password = password,
    )

fun createActivityAreaRequestDTO(
    redisId: String = TEST_REDIS_ID,
    primaryArea: String = TEST_PRIMARY_AREA,
    workplaceArea: String = TEST_WORKPLACE_AREA,
): ActivityAreaRequestDTO = ActivityAreaRequestDTO(
    redisId = redisId,
    primaryFormattedId = primaryArea,
    workplaceFormattedId = workplaceArea,
)

fun createKaKaoTokenResponseDTO(
    token: String = TEST_TOKEN_TYPE,
    accessToken: String = TEST_ACCESS_TOKEN,
    refreshToken: String = TEST_REFRESH_TOKEN,
    idToken: String = TEST_ID_TOKEN,
    expiresIn: Int = 0,
    refreshExpiresIn: Int = 0,
): KaKaoTokenResponseDTO = KaKaoTokenResponseDTO(
    tokenType = token,
    accessToken = accessToken,
    refreshToken = refreshToken,
    expiresIn = expiresIn,
    refreshTokenExpiresIn = refreshExpiresIn,
    idToken = idToken,
)
