package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.member.command.application.dto.request.EmailRequestDTO
import com.bockerl.snailmember.member.command.application.dto.request.EmailVerifyRequestDTO
import com.bockerl.snailmember.member.command.application.dto.request.PhoneRequestDTO

interface RegistrationService {
    fun initiateRegistration(requestDTO: EmailRequestDTO): String

    fun verifyEmailCode(requestDTO: EmailVerifyRequestDTO): String

    fun createEmailRefreshCode(redisId: String)

    fun createPhoneVerificationCode(requestDTO: PhoneRequestDTO): String
}