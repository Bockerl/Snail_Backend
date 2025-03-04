package com.bockerl.snailmember.area.query.service

import com.bockerl.snailmember.area.query.dto.request.AreaKeywordRequestDTO
import com.bockerl.snailmember.area.query.dto.request.AreaPositionRequestDTO
import com.bockerl.snailmember.area.query.dto.response.AreaResponseDTO

interface QueryAreaService {
    fun selectAreaByKeyword(requestDTO: AreaKeywordRequestDTO): AreaResponseDTO

    fun selectAreaByPosition(requestDTO: AreaPositionRequestDTO): AreaResponseDTO
}