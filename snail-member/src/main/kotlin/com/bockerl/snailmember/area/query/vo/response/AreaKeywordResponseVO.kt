package com.bockerl.snailmember.area.query.vo.response

import com.bockerl.snailmember.area.query.vo.QueryEmdAreaVO
import com.bockerl.snailmember.area.query.vo.QuerySiggAreaVO
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class AreaKeywordResponseVO(
    @field:Schema(description = "시군구 검색 결과 리스트", type = "List")
    @JsonProperty("sigg_areas")
    val siggAreas: List<QuerySiggAreaVO>,
    @field:Schema(description = "읍면동(리) 검색 결과 리스트", type = "List")
    @JsonProperty("emd_areas")
    val emdAreas: List<QueryEmdAreaVO>,
)