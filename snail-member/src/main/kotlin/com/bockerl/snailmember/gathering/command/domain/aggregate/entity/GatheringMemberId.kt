package com.bockerl.snailmember.gathering.command.domain.aggregate.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class GatheringMemberId(
    @Column(name = "gathering_id", nullable = false)
    val gatheringId: Long,
    @Column(name = "member_id", nullable = false)
    val memberId: Long,
) : Serializable