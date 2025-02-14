package com.bockerl.snailmember.area.command.domain.repository

import com.bockerl.snailmember.area.command.domain.aggregate.entity.SidoArea
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SidoAreaRepository : JpaRepository<SidoArea, Long>
