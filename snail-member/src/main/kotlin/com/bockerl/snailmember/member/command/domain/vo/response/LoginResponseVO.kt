package com.bockerl.snailmember.member.command.domain.vo.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class LoginResponseVO(
    @field:Schema(nullable = false, required = true, description = "액세스 토큰", example = "sample-access-token")
    @JsonProperty("accessToken")
    val accessToken: String,
    @field:Schema(nullable = false, required = true, description = "리프레시 토큰", example = "sample-refresh-token")
    @JsonProperty("refreshToken")
    val refreshToken: String,
)