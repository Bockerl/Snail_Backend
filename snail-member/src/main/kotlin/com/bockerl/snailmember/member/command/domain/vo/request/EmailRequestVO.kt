package com.bockerl.snailmember.member.command.domain.vo.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class EmailRequestVO(
    @field:Schema(description = "닉네임", example = "Xin Xin", type = "String")
    @JsonProperty(namespace = "memberNickName")
    val memberNickName: String? = null,
    @field:Schema(description = "인증 이메일", example = "bockerl@gmail.com", type = "String")
    @JsonProperty(namespace = "memberEmail")
    val memberEmail: String? = null,
    @field:Schema(description = "생년월일", example = "2002-02-27", type = "LocalDate")
    @JsonProperty(namespace = "memberBirth")
    val memberBirth: LocalDate? = null,
)