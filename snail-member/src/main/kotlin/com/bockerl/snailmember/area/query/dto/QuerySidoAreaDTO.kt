package com.bockerl.snailmember.area.query.dto

data class QuerySidoAreaDTO(
    val sidoAreaId: Long,
    val sidoAreaAdmCode: String,
    val sidoAreaName: String,
) {
    val formattedId: String
        get() = "SID-${sidoAreaId?.toString()?.padStart(8, '0') ?: "00000000"}"
}