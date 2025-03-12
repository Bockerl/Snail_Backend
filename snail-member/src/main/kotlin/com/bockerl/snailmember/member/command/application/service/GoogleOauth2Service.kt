package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.member.command.application.dto.response.LoginResponseDTO

interface GoogleOauth2Service {
    fun googleLogin(code: String): LoginResponseDTO
}