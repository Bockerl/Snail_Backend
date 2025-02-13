package com.bockerl.snailmember.area.query.vo

import io.swagger.v3.oas.annotations.media.Schema

data class QuerySiggAreaVO(
    @field:Schema(description = "시군구 고유 번호(PK)", example = "Sig-00000001", type = "String")
    val siggAreaId: String,
    @field:Schema(description = "시도 고유 번호(FK)", example = "Sid-00000001", type = "String")
    val sidoAreaId: String,
    @field:Schema(description = "시군구 admCode", example = "11650", type = "String")
    val siggAreaAdmCode: String,
    @field:Schema(description = "시군구 이름", example = "서초구", type = "String")
    val siggAreaName: String,
    @field:Schema(description = "시군구 본명", example = "서울특별시 서초구", type = "String")
    val siggFullName: String,
)
