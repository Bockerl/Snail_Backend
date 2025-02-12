package com.bockerl.snailmember.area.query.dto

import com.bockerl.snailmember.area.command.domain.aggregate.entity.EmdAreas.ReeArea
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

class QueryEmdAreaDTO(
    val emdAreaId: Long,
    val siggAreaId: Long,
    val emdAreaAdmCode: String,
    val emdAreaName: String,
    val emdFullName: String,
    // Postgres에서 지원하는 타입인 JsonB를 활용, 컬럼이 binary 형식으로 저장
    @JdbcTypeCode(SqlTypes.JSON)
    val ReeAreas: List<ReeArea> = listOf(),
)