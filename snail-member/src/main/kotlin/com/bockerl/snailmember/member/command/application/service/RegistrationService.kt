package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.member.command.application.dto.request.ActivityAreaRegisterRequestDTO
import com.bockerl.snailmember.member.command.application.dto.request.EmailRequestDTO
import com.bockerl.snailmember.member.command.application.dto.request.EmailVerifyRequestDTO
import com.bockerl.snailmember.member.command.application.dto.request.PasswordRequestDTO
import com.bockerl.snailmember.member.command.application.dto.request.PhoneRequestDTO
import com.bockerl.snailmember.member.command.application.dto.request.PhoneVerifyRequestDTO

interface RegistrationService {
    fun initiateRegistration(
        requestDTO: EmailRequestDTO,
        idempotencyKey: String,
    ): String

    fun verifyEmailCode(
        requestDTO: EmailVerifyRequestDTO,
        idempotencyKey: String,
    ): String

    fun createEmailRefreshCode(
        redisId: String,
        idempotencyKey: String,
    )

    fun createPhoneVerificationCode(
        requestDTO: PhoneRequestDTO,
        idempotencyKey: String,
    ): String

    fun verifyPhoneCode(
        requestDTO: PhoneVerifyRequestDTO,
        idempotencyKey: String,
    ): String

    fun createPhoneRefreshCode(
        requestDTO: PhoneRequestDTO,
        idempotencyKey: String,
    ): String

    fun postPassword(
        requestDTO: PasswordRequestDTO,
        idempotencyKey: String,
    ): String

    fun postActivityArea(
        requestDTO: ActivityAreaRegisterRequestDTO,
        idempotencyKey: String,
    )
}