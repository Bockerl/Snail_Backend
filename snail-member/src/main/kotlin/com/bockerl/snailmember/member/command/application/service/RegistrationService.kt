package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.member.command.application.dto.request.*

interface RegistrationService {
    fun initiateRegistration(requestDTO: EmailRequestDTO): String

    fun verifyEmailCode(requestDTO: EmailVerifyRequestDTO): String

    fun createEmailRefreshCode(redisId: String)

    fun createPhoneVerificationCode(requestDTO: PhoneRequestDTO): String

    fun verifyPhoneCode(requestDTO: PhoneVerifyRequestDTO): String

    fun createPhoneRefreshCode(requestDTO: PhoneRequestDTO): String

    fun postPassword(requestDTO: PasswordRequestDTO): String

    fun postActivityArea(requestDTO: ActivityAreaRequestDTO)
}
