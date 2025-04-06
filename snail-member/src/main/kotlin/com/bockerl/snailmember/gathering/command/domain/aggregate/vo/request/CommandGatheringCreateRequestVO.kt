package com.bockerl.snailmember.gathering.command.domain.aggregate.vo.request

import com.bockerl.snailmember.gathering.command.domain.enums.GatheringType
import io.swagger.v3.oas.annotations.media.Schema

data class CommandGatheringCreateRequestVO(
    @field:Schema(description = "모임 제목", example = "달팽이", type = "String")
    var gatheringTitle: String?,
    @field:Schema(description = "모임 정보", example = "달팽이 좋아해요?", type = "String")
    var gatheringInformation: String?,
    @field:Schema(description = "모임 타입", example = "HOBBY", type = "String")
    var gatheringType: GatheringType,
    @field:Schema(description = "활동 지역", example = "서울", type = "String")
    var gatheringRegion: String,
    @field:Schema(description = "모임 인원 제한", example = "50", type = "Int")
    var gatheringLimit: Int = 50,
    @field:Schema(description = "회원번호", example = "MEM-00000001", type = "String")
    val memberId: String,
)