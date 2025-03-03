package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.member.command.application.dto.response.LoginResponseDTO

interface LineOauth2Service {
    fun lineLogin(code: String): LoginResponseDTO
}