package com.bockerl.snailmember.area.query.vo

import io.swagger.v3.oas.annotations.media.Schema

data class QuerySidoAreaVO(
    @field:Schema(description = "시도 고유 번호(PK)", example = "Sid-00000001", type = "String")
    val sidoAreaId: String,
    @field:Schema(description = "시도 admCode", example = "11", type = "String")
    val sidoAreaAdmCode: String,
    @field:Schema(description = "시도 이름", example = "서울특별시", type = "String")
    val sidoAreaName: String,
)