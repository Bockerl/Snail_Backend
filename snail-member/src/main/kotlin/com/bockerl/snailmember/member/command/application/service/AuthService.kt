package com.bockerl.snailmember.member.command.application.service

interface AuthService {
    fun createEmailVerificationCode(email: String)

    fun verifyEmailCode(
        email: String,
        verificationCode: String,
    )

    fun createPhoneVerificationCode(phoneNumber: String): String

    fun verifyPhoneCode(
        phoneNumber: String,
        verificationCode: String,
    )
}