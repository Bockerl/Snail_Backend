package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.member.command.application.dto.request.ActivityAreaRequestDTO
import com.bockerl.snailmember.member.command.application.dto.request.EmailRequestDTO
import com.bockerl.snailmember.member.command.application.dto.request.EmailVerifyRequestDTO
import com.bockerl.snailmember.member.command.application.dto.request.PasswordRequestDTO
import com.bockerl.snailmember.member.command.application.dto.request.PhoneRequestDTO
import com.bockerl.snailmember.member.command.application.dto.request.PhoneVerifyRequestDTO

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