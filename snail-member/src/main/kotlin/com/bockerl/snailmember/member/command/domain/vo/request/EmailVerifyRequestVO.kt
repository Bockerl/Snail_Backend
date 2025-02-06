package com.bockerl.snailmember.member.command.domain.vo.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class EmailVerifyRequestVO(
    @field:Schema(description = "이메일 인증 코드", example = "66821", type = "String")
    @JsonProperty(namespace = "verificationCode")
    val verificationCode: String? = null,
    @field:Schema(description = "해당 이메일", example = "bockerl@gmail.com", type = "String")
    @JsonProperty(namespace = "memberEmail")
    val memberEmail: String? = null,
)