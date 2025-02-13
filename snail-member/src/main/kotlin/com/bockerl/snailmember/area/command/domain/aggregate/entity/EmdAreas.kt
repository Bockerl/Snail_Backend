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

@Entity
@Table(name = "emd_areas")
class EmdAreas(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emd_areas_id")
    var emdAreasId: Long? = null,
    @Column(name = "sigg_areas_id")
    var siggAreasId: Long,
    @Column(name = "emd_areas_adm_code")
    var emdAreasAdmCode: String,
    @Column(name = "emd_areas_name")
    var emdAreasName: String,
) {
    companion object {
        fun create(
            siggAreasId: Long,
            admCode: String,
            name: String,
        ) = EmdAreas(
            emdAreasId = 0,
            siggAreasId = siggAreasId,
            emdAreasAdmCode = admCode,
            emdAreasName = name,
        )
    }
}