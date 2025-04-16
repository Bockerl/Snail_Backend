package com.bockerl.snailmember.gathering.command.application.service

import com.bockerl.snailmember.gathering.command.application.dto.*
import org.springframework.web.multipart.MultipartFile

interface CommandGatheringService {
    fun createGathering(
        commandGatheringCreateDTO: CommandGatheringCreateDTO,
        files: List<MultipartFile>,
    )

    fun updateGathering(
        commandGatheringUpdateDTO: CommandGatheringUpdateDTO,
        files: List<MultipartFile>,
    )

    fun deleteGathering(commandGatheringDeleteDTO: CommandGatheringDeleteDTO)

    fun updateGatheringAuthorization(commandGatheringAuthorizationUpdateDTO: CommandGatheringAuthorizationUpdateDTO)

    fun createGatheringMember(commandGatheringMemberCreateDTO: CommandGatheringMemberCreateDTO)

    fun deleteGatheringMember(commandGatheringMemberCreateDTO: CommandGatheringMemberCreateDTO)
}