package com.bockerl.snailmember.gathering.command.domain.repository

import com.bockerl.snailmember.gathering.command.domain.aggregate.entity.GatheringMember
import com.bockerl.snailmember.gathering.command.domain.aggregate.entity.GatheringMemberId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CommandGatheringMemberRepository : JpaRepository<GatheringMember, GatheringMemberId> {
    fun findByIdGatheringId(gatheringId: Long): List<GatheringMember>

    @Modifying
    @Query("UPDATE GatheringMember g SET g.active = false WHERE g.id.gatheringId = :gatheringId and g.active = true")
    fun updateActiveByGatheringId(gatheringId: Long?)
}