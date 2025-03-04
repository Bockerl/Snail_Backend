package com.bockerl.snailmember.area.query.dto.response

import com.bockerl.snailmember.area.query.dto.QueryEmdAreaDTO
import com.bockerl.snailmember.area.query.dto.QuerySiggAreaDTO

class AreaResponseDTO(
    val siggAreas: List<QuerySiggAreaDTO> = emptyList(),
    val emdReeAreas: List<QueryEmdAreaDTO> = emptyList(),
)