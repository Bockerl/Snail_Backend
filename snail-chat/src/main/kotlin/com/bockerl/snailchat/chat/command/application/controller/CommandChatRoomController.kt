package com.bockerl.snailchat.chat.command.application.controller

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomCreateRequestDTO
import com.bockerl.snailchat.chat.command.application.mapper.VoToDtoConverter
import com.bockerl.snailchat.chat.command.application.service.CommandChatRoomService
import com.bockerl.snailchat.chat.command.domain.aggregate.vo.request.CommandChatRoomDeleteRequestVo
import com.bockerl.snailchat.chat.command.domain.aggregate.vo.request.CommandChatRoomJoinRequestVo
import com.bockerl.snailchat.common.ResponseDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chatRoom")
class CommandChatRoomController(
    private val commandChatRoomService: CommandChatRoomService,
    private val voToDtoConverter: VoToDtoConverter,
) {
    @Operation(
        summary = "채팅방 생성(개인)",
        description = "사용자가 다른 사용자에게 DM 버튼을 누르면 채팅방이 생성된다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "개인 채팅방 생성 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CommandChatRoomCreateRequestDTO::class),
                    ),
                ],
            ),
        ],
    )
    @PostMapping("/create-personal/{memberId}/{memberNickname}/{memberPhoto}")
    fun createPersonalChatRoom(
        @PathVariable memberId: String, // 향후 Principal을 통해 memberId, memberNickname, memberPhoto
        @PathVariable memberNickname: String,
        @PathVariable memberPhoto: String,
    ): ResponseDTO<*> {
        val commandChatRoomCreateRequestDto = CommandChatRoomCreateRequestDTO(memberId, memberNickname, memberPhoto)

        commandChatRoomService.createPersonalChatRoom(commandChatRoomCreateRequestDto)

        return ResponseDTO.ok(null)
    }

    @Operation(
        summary = "채팅방 생성(그룹)",
        description = "모임 및 모임 일정 생성시 그룹 채팅방 생성",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "그룹 채팅방 생성 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CommandChatRoomCreateRequestDTO::class),
                    ),
                ],
            ),
        ],
    )
    @PostMapping("/create-group/{memberId}/{memberNickname}/{memberPhoto}")
    fun createGroupChatRoom(
        @PathVariable memberId: String, // 향후 Principal을 통해 memberId, memberNickname, memberPhoto
        @PathVariable memberNickname: String,
        @PathVariable memberPhoto: String,
    ): ResponseDTO<*> {
        val commandChatRoomCreateRequestDto = CommandChatRoomCreateRequestDTO(memberId, memberNickname, memberPhoto)

        commandChatRoomService.createGroupChatRoom(commandChatRoomCreateRequestDto)

        return ResponseDTO.ok(null)
    }

    @Operation(
        summary = "개인 채팅방 삭제 & 나가기",
        description = "사용자가 개인 채팅방을 삭제 or 나가기 한다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "개인 채팅방 삭제 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CommandChatRoomCreateRequestDTO::class),
                    ),
                ],
            ),
        ],
    )
    @DeleteMapping("/delete-personal/{memberId}/{memberNickname}/{memberPhoto}") // 향후 msa 설계 완료 후 수정할 계획
    fun deletePersonalChatRoom(
        @RequestBody commandChatRoomDeleteRequestVo: CommandChatRoomDeleteRequestVo,
        @PathVariable memberId: String, // 향후 Principal을 통해 memberId, memberNickname
        @PathVariable memberNickname: String,
        @PathVariable memberPhoto: String,
    ): ResponseDTO<*> {
        val commandChatRoomDeleteRequestDto =
            voToDtoConverter.commandChatRoomDeleteRequestVoTODto(commandChatRoomDeleteRequestVo, memberId, memberNickname, memberPhoto)

        commandChatRoomService.deletePersonalChatRoom(commandChatRoomDeleteRequestDto)

        return ResponseDTO.ok(null)
    }

    @Operation(
        summary = "그룹 채팅방 삭제 & 나가기",
        description = "사용자가 그룹 채팅방을 삭제 or 나가기 한다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "그룹 채팅방 삭제 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CommandChatRoomCreateRequestDTO::class),
                    ),
                ],
            ),
        ],
    )
    @DeleteMapping("/delete-group/{memberId}/{memberNickname}/{memberPhoto}") // 향후 msa 설계 완료 후 수정할 계획
    fun deleteGroupChatRoom(
        @RequestBody commandChatRoomDeleteRequestVo: CommandChatRoomDeleteRequestVo,
        @PathVariable memberId: String, // 향후 Principal을 통해 memberId, memberNickname
        @PathVariable memberNickname: String,
        @PathVariable memberPhoto: String,
    ): ResponseDTO<*> {
        val commandChatRoomDeleteRequestDto =
            voToDtoConverter.commandChatRoomDeleteRequestVoTODto(commandChatRoomDeleteRequestVo, memberId, memberNickname, memberPhoto)

        commandChatRoomService.deleteGroupChatRoom(commandChatRoomDeleteRequestDto)

        return ResponseDTO.ok(null)
    }

    @Operation(
        summary = "그룹 채팅방 참가",
        description = "모임 및 모임 참여시 그룹 채팅방 참가",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "그룹 채팅방 참가 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CommandChatRoomCreateRequestDTO::class),
                    ),
                ],
            ),
        ],
    )
    @PostMapping("/join/{memberId}/{memberNickname}/{memberPhoto}")
    fun joinGroupChatRoom(
        @RequestBody commandChatRoomJoinRequestVo: CommandChatRoomJoinRequestVo,
        @PathVariable memberId: String, // 향후 Principal을 통해 memberId, memberNickname
        @PathVariable memberNickname: String,
        @PathVariable memberPhoto: String,
    ): ResponseDTO<*> {
        val commandChatRoomJoinRequestDto =
            voToDtoConverter.commandChatRoomJoinRequestVoToDto(commandChatRoomJoinRequestVo, memberId, memberNickname, memberPhoto)

        commandChatRoomService.joinGroupChatRoom(commandChatRoomJoinRequestDto)

        return ResponseDTO.ok(null)
    }
}