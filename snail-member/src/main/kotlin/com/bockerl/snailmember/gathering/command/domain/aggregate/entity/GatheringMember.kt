@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.gathering.command.domain.aggregate.entity

import com.bockerl.snailmember.gathering.command.domain.enums.GatheringRole
import jakarta.persistence.*

@Entity
@Table(name = "Gathering_member")
class GatheringMember(
    @EmbeddedId
    val id: GatheringMemberId,
    @MapsId("gatheringId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id")
    val gathering: Gathering,
    @Column(name = "gathering_role", nullable = false, length = 255)
    var gatheringRole: GatheringRole,
    @Column(name = "active", nullable = false)
    var active: Boolean = true,
)