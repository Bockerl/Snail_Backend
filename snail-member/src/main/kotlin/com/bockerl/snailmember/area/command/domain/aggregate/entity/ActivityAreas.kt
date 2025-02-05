package com.bockerl.snailmember.area.command.domain.aggregate.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table

@Entity
@Table(name = "activity_ares")
class ActivityAreas(
    @EmbeddedId
    val id: ActivityId,
    @Enumerated(EnumType.STRING)
    @Column(name = "area_type", nullable = false)
    val areaType: AreaType,
) {
    @Embeddable
    data class ActivityId(
        @Column(name = "member_id")
        val memberId: Long,
        @Column(name = "emd_areas_id")
        val emdAreasId: Long,
    )
}