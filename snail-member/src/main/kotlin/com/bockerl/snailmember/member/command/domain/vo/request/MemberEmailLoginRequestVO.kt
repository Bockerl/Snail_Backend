package com.bockerl.snailmember.member.command.domain.vo.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class MemberEmailLoginRequestVO(
    @field:Schema
    @JsonProperty("memberEmail")
    val memberEmail: String,
    @field:Schema
    @JsonProperty("memberPassword")
    val memberPassword: String,
    @field:Schema
    @JsonProperty("eventId")
    val eventId: String,
)