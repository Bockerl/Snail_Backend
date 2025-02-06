package com.bockerl.snailmember.member.command.application.controller

import com.bockerl.snailmember.common.ResponseDTO
import com.bockerl.snailmember.member.command.application.mapper.AuthConverter
import com.bockerl.snailmember.member.command.application.service.AuthService
import com.bockerl.snailmember.member.command.domain.vo.request.EmailRequestVO
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val authConverter: AuthConverter,
) {
    @PostMapping("/verification/trial/email")
    fun postEmailVerificationCode(
        @RequestBody emailRequestVO: EmailRequestVO,
    ): ResponseDTO<*> {
        authService.createEmailVerificationCode(emailRequestVO)
        return ResponseDTO.ok("메일 인증 코드가 보내졌습니다.")
    }

    @PostMapping("/verification/trial/email/refresh")
    fun postEmailRefreshCode(
        @RequestBody emailRequestVO: EmailRequestVO,
    ): ResponseDTO<*> {
        authService.createEmailRefreshCode(emailRequestVO)
        return ResponseDTO.ok("메일 인증 코드가 재발급되었습니다.")
    }
}