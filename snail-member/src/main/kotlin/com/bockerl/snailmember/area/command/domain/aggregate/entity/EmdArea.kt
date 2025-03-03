/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.area.command.domain.aggregate.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "emd_area")
class EmdArea(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emd_area_id")
    var emdAreaId: Long,
    @Column(name = "sigg_area_id")
    var siggAreaId: Long,
    @Column(name = "emd_area_adm_code")
    var emdAreaAdmCode: String,
    @Column(name = "emd_area_name")
    var emdAreaName: String,
    @Column(name = "emd_full_name")
    var emdFullName: String,
    // Postgres에서 지원하는 타입인 JsonB를 활용, 컬럼이 binary 형식으로 저장
    @Column(name = "ree_area", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    var ReeAreas: List<ReeArea> = listOf(),
) {
    data class ReeArea(val reeAreaAdmCode: String, val reeAreasName: String, val fullName: String)

    companion object {
        fun create(siggAreaId: Long, admCode: String, areaName: String, fullName: String) = EmdArea(
            emdAreaId = 0,
            siggAreaId = siggAreaId,
            emdAreaAdmCode = admCode,
            emdAreaName = areaName,
            emdFullName = fullName,
        )
    }
}
