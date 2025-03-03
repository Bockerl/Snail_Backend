package com.bockerl.snailmember.member.command.application.dto.response

data class LoginResponseDTO(
    val accessToken: String,
    val refreshToken: String,
)