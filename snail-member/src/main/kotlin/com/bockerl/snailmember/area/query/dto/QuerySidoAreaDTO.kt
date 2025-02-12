package com.bockerl.snailmember.area.query.dto

import io.swagger.v3.oas.annotations.media.Schema

data class QuerySidoAreaDTO(
    @field:Schema(description = "시도 고유 번호(PK)", example = "1", type = "Long")
    val sidoAreaId: Long? = null,
    @field:Schema(description = "시도 admCode", example = "11", type = "String")
    val sidoAreaAdmCode: String? = null,
    @field:Schema(description = "시도 이름", example = "서울특별시", type = "String")
    val sidoAreaName: String? = null,
) {
    val formattedId: String
        get() = "SID-${sidoAreaId?.toString()?.padStart(8, '0') ?: "00000000"}"
}