package com.bockerl.snailmember.member.command.domain.vo.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class PhoneRequestVO(
    @field:Schema(description = "redis 저장 Id(UUID)", example = "temp:member:as12f23", type = "String")
    @JsonProperty("redisId")
    val redisId: String? = null,
    @field:Schema(description = "휴대폰 번호", example = "010-0000-0000", type = "String")
    @JsonProperty("phoneNumber")
    val phoneNumber: String? = null,
)