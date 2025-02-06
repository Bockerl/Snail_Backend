package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.member.command.domain.vo.request.EmailRequestVO
import com.bockerl.snailmember.member.command.domain.vo.request.EmailVerifyRequestVO

interface AuthService {
    fun createEmailVerificationCode(emailRequestVO: EmailRequestVO)

    fun createEmailRefreshCode(emailRequestVO: EmailRequestVO)

    fun verifyEmailCode(emailVerifyRequestVO: EmailVerifyRequestVO)
}