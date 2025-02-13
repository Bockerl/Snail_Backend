package com.bockerl.snailmember.member.command.application.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

class EmailVerifyRequestDTO(
    @field:Schema(description = "이메일 인증 코드", example = "66821", type = "String")
    @JsonProperty(namespace = "verificationCode")
    val verificationCode: String,
    @field:Schema(description = "redis 저장 Id(UUID)", example = "temp:member:as12f23", type = "String")
    @JsonProperty(namespace = "redisId")
    val redisId: String,
)
