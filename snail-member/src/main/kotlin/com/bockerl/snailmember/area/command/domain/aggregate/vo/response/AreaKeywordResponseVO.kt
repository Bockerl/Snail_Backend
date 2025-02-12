package com.bockerl.snailmember.area.command.domain.aggregate.vo.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class AreaKeywordResponseVO(
    @field:Schema(description = "지명 풀네임(읍면동)", example = "서울시 ")
    @JsonProperty("fullName")
    val fullName: String,
)