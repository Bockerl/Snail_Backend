/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.area.command.domain.aggregate.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "activity_area")
@EntityListeners(AuditingEntityListener::class)
class ActivityArea(
    @EmbeddedId
    val id: ActivityId? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "area_type", nullable = false)
    val areaType: AreaType? = null,
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime? = null,
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime? = null,
) {
    @Embeddable
    data class ActivityId(
        @Column(name = "member_id")
        val memberId: Long,
        @Column(name = "emd_area_id")
        val emdAreasId: Long,
    )
}
