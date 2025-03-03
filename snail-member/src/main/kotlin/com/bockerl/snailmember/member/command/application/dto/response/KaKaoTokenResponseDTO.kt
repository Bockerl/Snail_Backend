package com.bockerl.snailmember.member.command.application.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class KaKaoTokenResponseDTO(
    @JsonProperty("token_type")
    val tokenType: String,
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("id_token")
    val idToken: String,
    @JsonProperty("expires_in")
    val expiresIn: Int,
    @JsonProperty("refresh_token")
    val refreshToken: String,
    @JsonProperty("refresh_token_expires_in")
    val refreshTokenExpiresIn: Int,
)