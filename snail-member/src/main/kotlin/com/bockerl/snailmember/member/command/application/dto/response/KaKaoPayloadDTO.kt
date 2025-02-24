package com.bockerl.snailmember.member.command.application.dto.response

data class KaKaoPayloadDTO(
    val id: Long,
    val email: String?,
    val nickname: String?,
    val birthday: String?,
    val birthyear: String?,
)
