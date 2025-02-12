package com.bockerl.snailmember.area.query.service

import com.bockerl.snailmember.area.query.dto.request.AreaKeywordRequestDTO
import com.bockerl.snailmember.area.query.dto.response.AreaKeywordResponseDTO
import com.bockerl.snailmember.area.query.repository.ActivityAreasMapper
import com.bockerl.snailmember.area.query.repository.EmdAreasMapper
import com.bockerl.snailmember.area.query.repository.SiggAreasMapper
import org.springframework.stereotype.Service

@Service
class QueryServiceImpl(
    private val activityAreasMapper: ActivityAreasMapper,
    private val emdAreasMapper: EmdAreasMapper,
    private val siggAreasMapper: SiggAreasMapper,
) : QueryAreaService {
    override fun selectAreaByKeyword(requestDTO: AreaKeywordRequestDTO): AreaKeywordResponseDTO {
        val keyword = requestDTO.areaSearchKeyword
        // 시군구, 읍면동(리) repository를 돌면서 fullName 중 keyword가 들어가는 것들을 List로 반환
        val siggAreas = siggAreasMapper.selectSiggAreasByKeyword(keyword)
        val emdAreas = emdAreasMapper.selectEmdAreasByKeyword(keyword)
        return AreaKeywordResponseDTO(
            fullName = "",
        )
    }
}