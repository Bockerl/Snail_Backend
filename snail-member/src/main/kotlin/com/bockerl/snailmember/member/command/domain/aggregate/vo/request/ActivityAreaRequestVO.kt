package com.bockerl.snailmember.member.command.domain.aggregate.vo.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

class ActivityAreaRequestVO(
    @field:Schema(description = "redis 저장 Id(UUID)", example = "as12f23", type = "String")
    @JsonProperty("redisId")
    val redisId: String? = null,
    @field:Schema(description = "주 지역", example = "as12f23", type = "String")
    @JsonProperty("redisId")
    val primaryAdmCode: String? = null,
    val workplaceAdmCode: String? = null,
)
