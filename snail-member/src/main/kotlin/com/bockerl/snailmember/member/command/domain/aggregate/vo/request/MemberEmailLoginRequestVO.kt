package com.bockerl.snailmember.member.command.domain.aggregate.vo.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class MemberEmailLoginRequestVO(
    @field:Schema
    @JsonProperty("memberEmail")
    val memberEmail: String,
    @field:Schema
    @JsonProperty("memberPassword")
    val memberPassword: String,
)