package com.bockerl.snailmember.area.query.service

import com.bockerl.snailmember.area.query.dto.request.AreaKeywordRequestDTO
import com.bockerl.snailmember.area.query.dto.request.AreaPositionRequestDTO
import com.bockerl.snailmember.area.query.dto.response.AreaKeywordResponseDTO
import com.bockerl.snailmember.area.query.dto.response.AreaPositionResponseDTO

interface QueryAreaService {
    fun selectAreaByKeyword(requestDTO: AreaKeywordRequestDTO): AreaKeywordResponseDTO

    fun selectAreaByPosition(requestDTO: AreaPositionRequestDTO): AreaPositionResponseDTO
}