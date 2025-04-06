package com.bockerl.snailmember.gathering.command.domain.repository

import com.bockerl.snailmember.gathering.command.domain.aggregate.entity.Gathering
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CommandGatheringRepository : JpaRepository<Gathering, Long>