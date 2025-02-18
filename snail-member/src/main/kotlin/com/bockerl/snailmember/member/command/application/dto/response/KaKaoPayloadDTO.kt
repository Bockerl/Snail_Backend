package com.bockerl.snailmember.member.command.application.dto.response

data class KaKaoIdTokenPayloadDTO(
    val iss: String,
    val aud: String,
    val sub: String,
    val email: String?,
    val nickname: String?,
    val birthday: String?,
    val birthyear: String?,
)
