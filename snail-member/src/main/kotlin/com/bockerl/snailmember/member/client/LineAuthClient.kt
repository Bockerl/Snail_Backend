package com.bockerl.snailmember.member.client

import com.bockerl.snailmember.member.command.application.dto.response.LineTokenResponseDTO
import com.bockerl.snailmember.member.command.config.LineOauth2FeignConfig
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    name = "lineAuthClient",
    url = "https://api.line.me",
    configuration = [LineOauth2FeignConfig::class],
)
interface LineAuthClient {
    @PostMapping(
        value = ["/oauth2/v2.1/token"],
        consumes = ["application/x-www-form-urlencoded"],
    )
    fun getAccessToken(
        @RequestBody formData: String,
    ): LineTokenResponseDTO
}