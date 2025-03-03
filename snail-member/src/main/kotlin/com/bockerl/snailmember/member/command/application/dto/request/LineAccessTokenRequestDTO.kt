package com.bockerl.snailmember.member.command.application.dto.request

import com.fasterxml.jackson.annotation.JsonProperty

data class LineAccessTokenRequestDTO(
    @JsonProperty("grant_type")
    val grantType: String = "authorization_code",
    @JsonProperty("code")
    val code: String,
    @JsonProperty("redirect_uri")
    val redirectUri: String,
    @JsonProperty("client_id")
    val clientId: String,
    @JsonProperty("client_secret")
    val clientSecret: String,
)