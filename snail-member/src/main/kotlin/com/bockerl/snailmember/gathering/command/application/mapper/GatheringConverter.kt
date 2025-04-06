package com.bockerl.snailmember.gathering.command.application.mapper

import com.bockerl.snailmember.gathering.command.application.dto.CommandGatheringCreateDTO
import com.bockerl.snailmember.gathering.command.application.dto.CommandGatheringDeleteDTO
import com.bockerl.snailmember.gathering.command.application.dto.CommandGatheringUpdateDTO
import com.bockerl.snailmember.gathering.command.domain.aggregate.vo.request.CommandGatheringCreateRequestVO
import com.bockerl.snailmember.gathering.command.domain.aggregate.vo.request.CommandGatheringDeleteRequestVO
import com.bockerl.snailmember.gathering.command.domain.aggregate.vo.request.CommandGatheringUpdateRequestVO
import org.springframework.stereotype.Component

@Component
class GatheringConverter {
    fun createRequestVOToDTO(
        requestVO: CommandGatheringCreateRequestVO,
        idempotencyKey: String,
    ): CommandGatheringCreateDTO =
        CommandGatheringCreateDTO(
            gatheringTitle = requestVO.gatheringTitle,
            gatheringInformation = requestVO.gatheringInformation,
            gatheringType = requestVO.gatheringType,
            gatheringRegion = requestVO.gatheringRegion,
            gatheringLimit = requestVO.gatheringLimit,
            memberId = requestVO.memberId,
            idempotencyKey = idempotencyKey,
        )

    fun updateRequestVOToDTO(
        requestVO: CommandGatheringUpdateRequestVO,
        idempotencyKey: String,
    ): CommandGatheringUpdateDTO =
        CommandGatheringUpdateDTO(
            gatheringId = requestVO.gatheringId,
            gatheringInformation = requestVO.gatheringInformation,
            gatheringType = requestVO.gatheringType,
            gatheringRegion = requestVO.gatheringRegion,
            gatheringLimit = requestVO.gatheringLimit,
            memberId = requestVO.memberId,
            idempotencyKey = idempotencyKey,
            deleteFilesIds = requestVO.deleteFilesIds,
        )

    fun deleteRequestVOToDTO(
        requestVO: CommandGatheringDeleteRequestVO,
        idempotencyKey: String,
    ): CommandGatheringDeleteDTO =
        CommandGatheringDeleteDTO(
            gatheringId = requestVO.gatheringId,
            memberId = requestVO.memberId,
            idempotencyKey = idempotencyKey,
        )
}