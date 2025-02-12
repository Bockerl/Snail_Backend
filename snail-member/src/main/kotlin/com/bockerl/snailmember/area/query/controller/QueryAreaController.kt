package com.bockerl.snailmember.area.query.controller

import com.bockerl.snailmember.area.command.application.mapper.AreaConverter
import com.bockerl.snailmember.area.command.domain.aggregate.vo.request.AreaKeywordRequestVO
import com.bockerl.snailmember.area.query.service.QueryAreaService
import com.bockerl.snailmember.common.ResponseDTO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/area")
class QueryAreaController(
    private val queryAreaService: QueryAreaService,
    private val areaConverter: AreaConverter,
) {
    @GetMapping("/keyword")
    fun getAreaByKeyword(
        @RequestBody requestVO: AreaKeywordRequestVO,
    ): ResponseDTO<*> {
        // 유효성 검사
        requestVO.apply {
            requestVO.isValid()
        }
        // DTO 변환
        val requestDTO = areaConverter.areaKeywordRequestVOToDTO(requestVO)
        // 서비스 결과 DTO
        val responseDTO = queryAreaService.selectAreaByKeyword(requestDTO)
        // VO 변환
        val responseVO = areaConverter.areaKeywordResponseDTOToVO(responseDTO)
        return ResponseDTO.ok(responseVO)
    }
}