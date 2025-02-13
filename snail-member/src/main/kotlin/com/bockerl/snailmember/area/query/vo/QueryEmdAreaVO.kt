package com.bockerl.snailmember.area.query.vo

import io.swagger.v3.oas.annotations.media.Schema

data class QueryEmdAreaVO(
    @field:Schema(description = "읍면동 고유 번호(PK)", example = "Emd-00000001", type = "String")
    val emdAreaId: String,
    @field:Schema(description = "읍면동 고유 번호(PK)", example = "Emd-00000001", type = "String")
    val siggAreaId: String,
    @field:Schema(description = "읍면동 admCode", example = "11680108", type = "String")
    val emdAreaAdmCode: String,
    @field:Schema(description = "읍면동 이름", example = "논현동", type = "String")
    val emdAreaName: String,
    @field:Schema(description = "읍면동 본명", example = "서울특별시 강남구 논현동", type = "String")
    val emdFullName: String,
    @field:Schema(description = "리 배열", type = "List")
    val reeAreas: List<QueryReeAreaVO> = listOf(),
)
