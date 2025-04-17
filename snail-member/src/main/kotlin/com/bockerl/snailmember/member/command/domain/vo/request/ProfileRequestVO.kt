package com.bockerl.snailmember.member.command.domain.vo.request

import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.Gender
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class ProfileRequestVO(
    @field:Schema(description = "변경할 닉네임", example = "Hammer", type = "String")
    @JsonProperty("memberNickname")
    val nickName: String?,
    @field:Schema(description = "변경할 성별", example = "FEMALE", type = "String")
    @JsonProperty("memberGender")
    val gender: Gender?,
    @field:Schema(description = "변경할 생년월일", example = "2002-01-31", type = "LocalDate")
    @JsonProperty("memberNickname")
    val birth: LocalDate?,
    @field:Schema(description = "변경할 자기소개", example = "새로운 자기소개", type = "String")
    @JsonProperty("selfIntroduction")
    val selfIntroduction: String?,
    @field:Schema(description = "이벤트 ID(UUID)", example = "UUID", type = "String")
    @JsonProperty("eventId")
    val eventId: String? = null,
)