package com.bockerl.snailmember.area.command.domain.repository

import com.bockerl.snailmember.area.command.domain.aggregate.entity.EmdAreas
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EmdAreasRepository : JpaRepository<EmdAreas, Long> {
    fun findEmdAreasByEmdAreaAdmCode(admCode: String): EmdAreas?
}