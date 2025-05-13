package com.bockerl.snailmember.area.command.application.mapper

import com.bockerl.snailmember.area.query.dto.request.AreaKeywordRequestDTO
import com.bockerl.snailmember.area.query.dto.request.AreaPositionRequestDTO
import com.bockerl.snailmember.area.query.dto.response.AreaKeywordResponseDTO
import com.bockerl.snailmember.area.query.dto.response.AreaPositionResponseDTO
import com.bockerl.snailmember.area.query.vo.QueryEmdAreaVO
import com.bockerl.snailmember.area.query.vo.QueryReeAreaVO
import com.bockerl.snailmember.area.query.vo.QuerySiggAreaVO
import com.bockerl.snailmember.area.query.vo.request.AreaKeywordRequestVO
import com.bockerl.snailmember.area.query.vo.request.AreaPositionRequestVO
import com.bockerl.snailmember.area.query.vo.response.AreaKeywordResponseVO
import com.bockerl.snailmember.area.query.vo.response.AreaPositionResponseVO
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.command.application.dto.request.ActivityAreaRequestDTO
import com.bockerl.snailmember.member.command.domain.vo.request.ActivityAreaRequestVO
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class AreaConverter {
    private val logger = KotlinLogging.logger {}

    // 지역 키워드 검색 요청 vo to dto
    fun areaKeywordRequestVOToDTO(requestVO: AreaKeywordRequestVO) =
        AreaKeywordRequestDTO(
            areaSearchKeyword = requestVO.searchKeyWord,
        )

    // 지역 키워드 검색 결과 dto to vo
    fun areaResponseDTOToVO(responseDTO: AreaKeywordResponseDTO): AreaKeywordResponseVO =
        AreaKeywordResponseVO(
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

    // 지역 위치 검색 요청 vo to dto
    fun areaPositionVOToDTO(requestVO: AreaPositionRequestVO): AreaPositionRequestDTO =
        AreaPositionRequestDTO(
            longitude = requestVO.validLongitude,
            latitude = requestVO.validLatitude,
        )

    // 지역 위치 검색 요청 dto to vo
    fun areaPositionDTOToVO(responseDTO: AreaPositionResponseDTO): AreaPositionResponseVO =
        AreaPositionResponseVO(
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

    // 활동지역 변경 혹은 oauth 회원을 위한 vo to dto
    fun activityAreaRequestVOToDTO(requestVO: ActivityAreaRequestVO): ActivityAreaRequestDTO {
        val primaryId = requestVO.primaryId

        if (primaryId.isNullOrBlank() ||
            !primaryId.startsWith("EMD") ||
            primaryId == requestVO.workplaceId
        ) {
            logger.warn { "잘못된 형식의 활동번호, PrimaryId: $primaryId WorkplaceId: ${requestVO.workplaceId}" }
            throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT, "활동지역PK가 유효하지 않습니다.")
        }
        return ActivityAreaRequestDTO(
            primaryId = requestVO.primaryId,
            workplaceId = requestVO.workplaceId,
        )
    }
}