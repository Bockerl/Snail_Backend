package com.bockerl.snailmember.area.command.domain.aggregate.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "sido_areas")
class SidoAreas(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sido_areas_id")
    val sidoAreasId: Long? = null,
    @Column(name = "sido_area_adm_code")
    val sidoAreasAdmCode: String,
    @Column(name = "sido_area_name")
    val sidoAreasName: String,
) {
    companion object {
        fun create(
            admCode: String,
            name: String,
        ) = SidoAreas(
            sidoAreasId = 0,
            sidoAreasAdmCode = admCode,
            sidoAreasName = name,
        )
    }
}