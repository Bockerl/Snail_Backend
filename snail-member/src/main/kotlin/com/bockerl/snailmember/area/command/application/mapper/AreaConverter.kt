package com.bockerl.snailmember.area.command.application.mapper

import com.bockerl.snailmember.area.query.dto.request.AreaKeywordRequestDTO
import com.bockerl.snailmember.area.query.dto.request.AreaPositionRequestDTO
import com.bockerl.snailmember.area.query.dto.response.AreaResponseDTO
import com.bockerl.snailmember.area.query.vo.QueryEmdAreaVO
import com.bockerl.snailmember.area.query.vo.QueryReeAreaVO
import com.bockerl.snailmember.area.query.vo.QuerySiggAreaVO
import com.bockerl.snailmember.area.query.vo.request.AreaKeywordRequestVO
import com.bockerl.snailmember.area.query.vo.request.AreaPositionRequestVO
import com.bockerl.snailmember.area.query.vo.response.AreaResponseVO
import org.springframework.stereotype.Component

@Component
class AreaConverter {
    // 지역 키워드 검색 요청 vo to dto
    fun areaKeywordRequestVOToDTO(requestVO: AreaKeywordRequestVO) =
        AreaKeywordRequestDTO(
            areaSearchKeyword = requestVO.searchKeyWord,
        )

    // 지역 위치 검색 요청 vo to dto
    fun areaPositionVOToDTO(requestVO: AreaPositionRequestVO): AreaPositionRequestDTO =
        AreaPositionRequestDTO(
            longitude = requestVO.validLongitude,
            latitude = requestVO.validLatitude,
        )

    // 지역 검색 결과 dto to vo
    fun areaResponseDTOToVO(responseDTO: AreaResponseDTO): AreaResponseVO =
        AreaResponseVO(
            siggAreas =
                responseDTO.siggAreas.map { siggDTO ->
                    QuerySiggAreaVO(
                        siggAreaId = siggDTO.formattedSiggId,
                        sidoAreaId = siggDTO.formattedSidoId,
                        siggAreaAdmCode = siggDTO.siggAreaAdmCode,
                        siggAreaName = siggDTO.siggAreaName,
                        siggFullName = siggDTO.siggFullName,
                    )
                },
            emdAreas =
                responseDTO.emdReeAreas.map { emdDTO ->
                    QueryEmdAreaVO(
                        emdAreaId = emdDTO.formattedEmdId,
                        siggAreaId = emdDTO.formattedSiggId,
                        emdAreaAdmCode = emdDTO.emdAreaAdmCode,
                        emdAreaName = emdDTO.emdAreaName,
                        emdFullName = emdDTO.emdFullName,
                        reeAreas =
                            emdDTO.reeAreas.map { reeDTO ->
                                QueryReeAreaVO(
                                    reeAreasName = reeDTO.reeAreasName,
                                    reeAreaAdmCode = reeDTO.reeAreaAdmCode,
                                    fullName = reeDTO.fullName,
                                )
                            },
                    )
                },
        )
}