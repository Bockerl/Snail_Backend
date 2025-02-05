package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.member.command.domain.vo.request.EmailRequestVO

interface AuthService {
    fun createEmailVerificationCode(emailRequestVO: EmailRequestVO)
}