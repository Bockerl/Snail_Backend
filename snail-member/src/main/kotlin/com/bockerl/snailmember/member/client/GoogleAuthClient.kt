package com.bockerl.snailmember.member.client

import com.bockerl.snailmember.member.command.application.dto.response.GoogleTokenResponseDTO
import com.bockerl.snailmember.member.command.config.Oauth2FeignConfig
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "googleAuthClient",
    url = "https://oauth2.googleapis.com",
    configuration = [Oauth2FeignConfig::class],
)
interface GoogleAuthClient {
    @PostMapping(
        value = ["/token"],
        consumes = ["application/x-www-form-urlencoded;charset=utf-8"],
    )
    fun getAccessToken(
        @RequestParam("grant_type") grantType: String = "authorization_code",
        @RequestParam("client_id") clientId: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam("code") code: String,
        @RequestParam("client_secret") clientSecret: String,
    ): GoogleTokenResponseDTO
}