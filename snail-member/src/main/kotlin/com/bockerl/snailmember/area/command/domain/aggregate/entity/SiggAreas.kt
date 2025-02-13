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
@Table(name = "sigg_areas")
class SiggAreas(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sigg_areas_id")
    var siggAreasId: Long? = null,
    @Column(name = "sido_areas_id")
    var sidoAreasId: Long,
    @Column(name = "sigg_areas_adm_code")
    var siggAreasAdmCode: String,
    @Column(name = "sigg_areas_name")
    var siggAreasName: String,
) {
    companion object {
        fun create(
            sidoAreasId: Long,
            admCode: String,
            name: String,
        ) = SiggAreas(
            siggAreasId = 0,
            sidoAreasId = sidoAreasId,
            siggAreasAdmCode = admCode,
            siggAreasName = name,
        )
    }
}