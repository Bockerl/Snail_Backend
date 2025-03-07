package com.bockerl.snailchat.chat.command.application.controller

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomRequestDto
import com.bockerl.snailchat.chat.command.application.mapper.VoToDtoConverter
import com.bockerl.snailchat.chat.command.application.service.CommandChatRoomService
import com.bockerl.snailchat.chat.command.domain.aggregate.vo.request.CommandChatRoomCreateRequestVo
import com.bockerl.snailchat.common.ResponseDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
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
        summary = "채팅방 생성",
        description = "사용자가 다른 사용자에게 DM 버튼을 누르면 채팅방이 생성된다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "채팅방 생성 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CommandChatRoomRequestDto::class),
                    ),
                ],
            ),
        ],
    )
    @PostMapping("/create")
    fun createChatRoom(
        @RequestBody commandChatRoomCreateRequestVo: CommandChatRoomCreateRequestVo,
    ): ResponseDto<*> {
        val commandChatRoomRequestDto = voToDtoConverter.commandChatRoomCreateRequestVoTODto(commandChatRoomCreateRequestVo)

        commandChatRoomService.createChatRoom(commandChatRoomRequestDto)

        return ResponseDto.ok(null)
    }
}