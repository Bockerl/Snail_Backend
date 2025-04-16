package com.bockerl.snailmember.gathering.command.domain.aggregate.vo.request

import com.bockerl.snailmember.gathering.command.domain.enums.GatheringRole
import io.swagger.v3.oas.annotations.media.Schema

data class CommandGatheringAuthorizationUpdateRequestVO(
    @field:Schema(description = "모임 번호", example = "GAT-00000001", type = "String")
    var gatheringId: String,
    @field:Schema(description = "회원번호", example = "MEM-00000001", type = "String")
    val memberId: String,
    @field:Schema(description = "모임 권한", example = "LEADER", type = "String")
    var gatheringRole: GatheringRole,
)