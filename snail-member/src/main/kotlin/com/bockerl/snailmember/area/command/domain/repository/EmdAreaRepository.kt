/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.area.command.domain.repository

import com.bockerl.snailmember.area.command.domain.aggregate.entity.EmdArea
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EmdAreaRepository : JpaRepository<EmdArea, Long> {
    fun findEmdAreasByEmdAreaAdmCode(admCode: String): EmdArea?
}