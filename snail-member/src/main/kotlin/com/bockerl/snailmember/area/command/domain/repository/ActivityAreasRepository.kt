package com.bockerl.snailmember.area.command.domain.repository

import com.bockerl.snailmember.area.command.domain.aggregate.entity.ActivityArea
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ActivityAreasRepository : JpaRepository<ActivityArea, ActivityArea.ActivityId>
