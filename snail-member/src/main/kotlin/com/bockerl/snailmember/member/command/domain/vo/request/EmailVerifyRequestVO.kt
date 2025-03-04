package com.bockerl.snailmember.member.command.domain.vo.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class EmailVerifyRequestVO(
    @field:Schema(description = "이메일 인증 코드", example = "66821", type = "String")
    @JsonProperty("verificationCode")
    val verificationCode: String? = null,
    @field:Schema(description = "redis 저장 Id(UUID)", example = "as12f23", type = "String")
    @JsonProperty("redisId")
    val redisId: String? = null,
)