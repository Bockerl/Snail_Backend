package com.bockerl.snailmember.gathering.command.domain.aggregate.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
class GatheringMemberId(
    @Column(name = "gathering_id", nullable = false)
    var gatheringId: Long,
    @Column(name = "member_id", nullable = false)
    var memberId: Long,
) : Serializable