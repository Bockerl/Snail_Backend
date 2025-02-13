package com.bockerl.snailmember.member.command.domain.aggregate.vo.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.sql.Timestamp

data class EmailRequestVO(
    @field:Schema(description = "닉네임", example = "Xin Xin", type = "String")
    @JsonProperty("memberNickName")
    val memberNickName: String? = null,
    @field:Schema(description = "인증 이메일", example = "bockerl@gmail.com", type = "String")
    @JsonProperty("memberEmail")
    val memberEmail: String? = null,
    @field:Schema(description = "생년월일", example = "1709049600000", type = "LocalDate")
    @JsonProperty("memberBirth")
    val memberBirth: Timestamp? = null,
)
