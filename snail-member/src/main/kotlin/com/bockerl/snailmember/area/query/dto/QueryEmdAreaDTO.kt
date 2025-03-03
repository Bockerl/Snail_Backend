package com.bockerl.snailmember.area.query.dto

import com.bockerl.snailmember.area.command.domain.aggregate.entity.EmdArea.ReeArea

data class QueryEmdAreaDTO(
    val emdAreaId: Long,
    val siggAreaId: Long,
    val emdAreaAdmCode: String,
    val emdAreaName: String,
    val emdFullName: String,
    val reeAreas: List<ReeArea> = listOf(),
) {
    val formattedEmdId: String
        get() = "Emd-${emdAreaId.toString().padStart(8, '0') ?: "00000000"}"

    val formattedSiggId: String
        get() = "Sig-${siggAreaId.toString().padStart(8, '0') ?: "00000000"}"
}