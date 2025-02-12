package com.bockerl.snailmember.area.query.repository

import com.bockerl.snailmember.area.query.dto.QueryEmdAreaDTO
import org.apache.ibatis.annotations.Mapper

@Mapper
interface EmdAreasMapper {
    fun selectEmdAreasByKeyword(keyword: String): List<QueryEmdAreaDTO>
}