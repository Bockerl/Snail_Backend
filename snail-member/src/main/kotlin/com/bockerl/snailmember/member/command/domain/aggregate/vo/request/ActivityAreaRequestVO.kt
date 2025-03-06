package com.bockerl.snailmember.member.command.domain.aggregate.vo.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class ActivityAreaRequestVO(
    @field:Schema(description = "회원 PK", example = "MEM-00000001", type = "String")
    @JsonProperty("memberId")
    val memberId: String? = null,
    @field:Schema(description = "메인 지역 PK", example = "EMD-00000001", type = "String")
    @JsonProperty("primaryId")
    val primaryId: String? = null,
    @field:Schema(description = "직장 지역", example = "EMD-00000002", type = "String")
    @JsonProperty("workplaceId")
    val workplaceId: String? = null,
)