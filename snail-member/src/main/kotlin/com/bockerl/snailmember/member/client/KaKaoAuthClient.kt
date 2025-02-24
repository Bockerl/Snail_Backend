package com.bockerl.snailmember.member.client

import com.bockerl.snailmember.member.command.application.dto.response.KaKaoTokenResponseDTO
import com.bockerl.snailmember.member.command.config.Oauth2FeignConfig
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "kakaoAuthClient",
    url = "https://kauth.kakao.com",
    configuration = [Oauth2FeignConfig::class],
)
interface KaKaoAuthClient {
    @PostMapping(
        value = ["/oauth/token"],
        consumes = ["application/x-www-form-urlencoded;charset=utf-8"],
    )
    fun getAccessToken(
        @RequestParam("grant_type") grantType: String = "authorization_code",
        @RequestParam("client_id") clientId: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam("code") code: String,
        @RequestParam("client_secret") clientSecret: String,
    ): KaKaoTokenResponseDTO
}
