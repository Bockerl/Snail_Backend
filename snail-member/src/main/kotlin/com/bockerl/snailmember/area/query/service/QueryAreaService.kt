package com.bockerl.snailmember.area.query.service

import com.bockerl.snailmember.area.query.dto.request.AreaKeywordRequestDTO
import com.bockerl.snailmember.area.query.dto.response.AreaKeywordResponseDTO

interface QueryAreaService {
    fun selectAreaByKeyword(requestDTO: AreaKeywordRequestDTO): AreaKeywordResponseDTO
}
