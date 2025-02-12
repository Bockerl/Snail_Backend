package com.bockerl.snailmember.area.query.repository

import com.bockerl.snailmember.area.query.dto.QuerySidoAreaDTO
import org.apache.ibatis.annotations.Mapper

@Mapper
interface SidoAreasMapper {
    fun selectSidoAreaBySidoAreaId(sidoAreaId: Long): QuerySidoAreaDTO?
}