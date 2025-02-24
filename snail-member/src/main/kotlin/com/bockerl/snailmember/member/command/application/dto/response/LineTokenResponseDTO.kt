package com.bockerl.snailmember.member.command.application.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class LineTokenResponseDTO(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("expires_in")
    val expiresIn: Int,
    @JsonProperty("id_token")
    val idToken: String,
    @JsonProperty("refresh_token")
    val refreshToken: String,
    @JsonProperty("scope")
    val scope: String,
    @JsonProperty("token_type")
    val tokenType: String,
)
