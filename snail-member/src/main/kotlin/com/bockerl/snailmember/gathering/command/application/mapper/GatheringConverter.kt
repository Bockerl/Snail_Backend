package com.bockerl.snailmember.gathering.command.application.mapper

import com.bockerl.snailmember.gathering.command.application.dto.*
import com.bockerl.snailmember.gathering.command.domain.aggregate.vo.request.*
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

    fun updateAuthorizationRequestVOToDTO(
        requestVO: CommandGatheringAuthorizationUpdateRequestVO,
        idempotencyKey: String,
    ): CommandGatheringAuthorizationUpdateDTO =
        CommandGatheringAuthorizationUpdateDTO(
            gatheringId = requestVO.gatheringId,
            memberId = requestVO.memberId,
            gatheringRole = requestVO.gatheringRole,
            idempotencyKey = idempotencyKey,
        )

    fun memberRequestVOToDTO(
        gatheringId: String,
        memberId: String,
        idempotencyKey: String,
    ): CommandGatheringMemberCreateDTO =
        CommandGatheringMemberCreateDTO(
            gatheringId = gatheringId,
            memberId = memberId,
            idempotencyKey = idempotencyKey,
        )

    fun deleteMemberRequestVOToDTO(
        requestVO: CommandGatheringMemberDeleteRequestVO,
        idempotencyKey: String,
    ): CommandGatheringMemberCreateDTO =
        CommandGatheringMemberCreateDTO(
            gatheringId = requestVO.gatheringId,
            memberId = requestVO.memberId,
            idempotencyKey = idempotencyKey,
        )
}