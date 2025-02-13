package com.bockerl.snailmember.area.command.domain.repository

import com.bockerl.snailmember.area.command.domain.aggregate.entity.EmdArea
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EmdAreasRepository : JpaRepository<EmdArea, Long> {
    fun findEmdAreasByEmdAreaAdmCode(admCode: String): EmdArea?
}
