package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.member.command.domain.aggregate.entity.tempMember.VerificationType

interface AuthService {
    fun createEmailVerificationCode(email: String)

    fun createPhoneVerificationCode(phoneNumber: String): String

    fun verifyCode(
        thing: String,
        verificationCode: String,
        type: VerificationType,
    )
}