package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.member.command.application.dto.response.LoginResponseDTO

interface KaKaoOauth2Service {
    fun kakaoLogin(code: String): LoginResponseDTO
}
