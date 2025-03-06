package com.bockerl.snailmember.member.command.domain.vo.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class PasswordRequestVO(
    @field:Schema(description = "redis 저장 Id(UUID)", example = "temp:member:as12f23", type = "String")
    @JsonProperty("redisId")
    val redisId: String? = null,
    @field:Schema(description = "비밀번호", example = "bockerl#1", type = "String")
    @JsonProperty("password")
    val password: String? = null,
)