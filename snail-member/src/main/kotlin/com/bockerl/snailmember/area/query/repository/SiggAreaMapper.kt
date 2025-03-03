package com.bockerl.snailmember.area.query.repository

import com.bockerl.snailmember.area.query.dto.QuerySiggAreaDTO
import org.apache.ibatis.annotations.Mapper

@Mapper
interface SiggAreaMapper {
    fun selectSiggAreasByKeyword(keyword: String): List<QuerySiggAreaDTO>
}