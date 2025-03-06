/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.area.command.domain.repository

import com.bockerl.snailmember.area.command.domain.aggregate.entity.ActivityArea
import com.bockerl.snailmember.area.command.domain.aggregate.entity.AreaType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface ActivityAreaRepository : JpaRepository<ActivityArea, ActivityArea.ActivityId> {
    @Query("SELECT a FROM ActivityArea a WHERE a.id.memberId = :memberId AND a.areaType = :type")
    fun findByMemberIdAndAreaType(
        @Param("memberId") memberId: Long,
        @Param("type") type: AreaType,
    ): ActivityArea?

    @Modifying
    @Transactional
    @Query("DELETE FROM ActivityArea a WHERE a.id.memberId = :memberId AND a.areaType = :type")
    fun deleteByMemberIdAndAreaType(
        memberId: Long,
        type: AreaType,
    )
}