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
@Table(name = "sido_area")
class SidoAreas(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sido_area_id")
    val sidoAreaId: Long,
    @Column(name = "sido_area_adm_code")
    val sidoAreaAdmCode: String,
    @Column(name = "sido_area_name")
    val sidoAreaName: String,
) {
    companion object {
        fun create(
            admCode: String,
            name: String,
        ) = SidoAreas(
            sidoAreaId = 0,
            sidoAreaAdmCode = admCode,
            sidoAreaName = name,
        )
    }
}