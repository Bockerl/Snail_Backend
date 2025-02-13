package com.bockerl.snailmember.area.query.dto

class QuerySiggAreaDTO(
    val siggAreaId: Long,
    val sidoAreaId: Long,
    val siggAreaAdmCode: String,
    val siggAreaName: String,
    val siggFullName: String,
) {
    val formattedSiggId: String
        get() = "Sig-${siggAreaId.toString().padStart(8, '0')}"
    val formattedSidoId: String
        get() = "Sid-${sidoAreaId.toString().padStart(8, '0')}"
}
