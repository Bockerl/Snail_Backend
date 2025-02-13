package com.bockerl.snailmember.area.command.domain.aggregate.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "sigg_area")
class SiggArea(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sigg_area_id")
    var siggAreaId: Long,
    @Column(name = "sido_area_id")
    var sidoAreaId: Long,
    @Column(name = "sigg_area_adm_code")
    var siggAreaAdmCode: String,
    @Column(name = "sigg_area_name")
    var siggAreaName: String,
    @Column(name = "sigg_full_name")
    var siggFullName: String,
) {
    companion object {
        fun create(sidoAreaId: Long, admCode: String, areaName: String, fullName: String) = SiggArea(
            siggAreaId = 0,
            sidoAreaId = sidoAreaId,
            siggAreaAdmCode = admCode,
            siggAreaName = areaName,
            siggFullName = fullName,
        )
    }

    val formattedSiggId: String
        get() = "Sig-${siggAreaId.toString().padStart(8, '0')}"
    val formattedSidoId: String
        get() = "Sid-${sidoAreaId.toString().padStart(8, '0')}"
}
