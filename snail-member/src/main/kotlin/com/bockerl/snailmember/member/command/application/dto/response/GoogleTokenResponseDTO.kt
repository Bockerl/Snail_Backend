package com.bockerl.snailmember.member.command.application.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class GoogleTokenResponseDTO(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("id_token")
    val idToken: String,
    @JsonProperty("expires_in")
    val expiresIn: Int,
    @JsonProperty("refresh_token")
    val refreshToken: String? = null,
    @JsonProperty("scope")
    val scope: String,
    @JsonProperty("token_type")
    val tokenType: String,
)
