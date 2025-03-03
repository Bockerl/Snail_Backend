package com.bockerl.snailmember.area.query.service

import com.bockerl.snailmember.area.query.dto.request.AreaKeywordRequestDTO
import com.bockerl.snailmember.area.query.dto.response.AreaKeywordResponseDTO
import com.bockerl.snailmember.area.query.repository.ActivityAreaMapper
import com.bockerl.snailmember.area.query.repository.EmdAreaMapper
import com.bockerl.snailmember.area.query.repository.SiggAreaMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class QueryServiceImpl(
    private val activityAreasMapper: ActivityAreaMapper,
    private val emdAreasMapper: EmdAreaMapper,
    private val siggAreasMapper: SiggAreaMapper,
) : QueryAreaService {
    private val logger = KotlinLogging.logger {}

    override fun selectAreaByKeyword(requestDTO: AreaKeywordRequestDTO): AreaKeywordResponseDTO {
        logger.info { "키워드 기반 동네 검색 서비스 메서드 시작" }
        val keyword = requestDTO.areaSearchKeyword
        logger.info { "검색 키워드: $keyword" }
        // 시군구, 읍면동(리) repository를 돌면서 fullName 중 keyword가 들어가는 것들을 List로 반환
        val siggAreas = siggAreasMapper.selectSiggAreasByKeyword(keyword)
        logger.info { "조회된 시군구 리스트: $siggAreas" }
        val emdAreas = emdAreasMapper.selectEmdAreasByKeyword(keyword)
        logger.info { "조회된 읍면동 리스트: $emdAreas" }
        return AreaKeywordResponseDTO(
            siggAreas = siggAreas,
            emdReeAreas = emdAreas,
        )
    }
}