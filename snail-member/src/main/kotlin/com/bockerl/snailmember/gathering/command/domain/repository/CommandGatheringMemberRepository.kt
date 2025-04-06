package com.bockerl.snailmember.gathering.command.domain.repository

import com.bockerl.snailmember.gathering.command.domain.aggregate.entity.GatheringMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CommandGatheringMemberRepository : JpaRepository<GatheringMember, Long>