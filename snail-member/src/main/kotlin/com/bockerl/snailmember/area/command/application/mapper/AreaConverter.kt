package com.bockerl.snailmember.area.command.application.mapper

import com.bockerl.snailmember.area.command.domain.aggregate.vo.request.AreaKeywordRequestVO
import com.bockerl.snailmember.area.command.domain.aggregate.vo.response.AreaKeywordResponseVO
import com.bockerl.snailmember.area.query.dto.request.AreaKeywordRequestDTO
import com.bockerl.snailmember.area.query.dto.response.AreaKeywordResponseDTO
import org.springframework.stereotype.Component

@Component
class AreaConverter {
    // 지역 키워드 검색 요청 vo to dto
    fun areaKeywordRequestVOToDTO(requestVO: AreaKeywordRequestVO) =
        AreaKeywordRequestDTO(
            areaSearchKeyword = requestVO.searchKeyWord,
        )

    // 지역 키워드 검색 결과 dto to vo
    fun areaKeywordResponseDTOToVO(requestDTO: AreaKeywordResponseDTO): AreaKeywordResponseVO =
        AreaKeywordResponseVO(
            fullName = requestDTO.fullName,
        )
}