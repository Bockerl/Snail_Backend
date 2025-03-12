package com.bockerl.snailmember.area.query.repository

import com.bockerl.snailmember.area.query.dto.QueryEmdAreaDTO
import org.apache.ibatis.annotations.Mapper

@Mapper
interface EmdAreaMapper {
    // 읍면동 + 리 검색, TypeHandler 사용
    fun selectEmdAreasByKeyword(keyword: String): List<QueryEmdAreaDTO>

    fun selectEmdAreasByAdmCode(admCode: String): List<QueryEmdAreaDTO>
}