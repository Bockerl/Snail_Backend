@file:Suppress("ktlint:standard:import-ordering", "ktlint:standard:no-wildcard-imports")

package com.bockerl.snailchat.chat.command.application.controller

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDto
import com.bockerl.snailchat.chat.command.application.mapper.VoToDtoConverter
import com.bockerl.snailchat.chat.command.application.service.CommandChatMessageService
import com.bockerl.snailchat.chat.command.domain.aggregate.enums.CommandChatMessageType
import com.bockerl.snailchat.chat.command.domain.aggregate.vo.request.SendMessageRequestVo
import com.bockerl.snailchat.config.OpenApiBody
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Encoding
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.MediaType
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.simp.SimpMessageHeaderAccessor

@RestController
@RequestMapping("/api/chat")
class CommandChatMessageController(
    private val commandChatMessageService: CommandChatMessageService,
    private val voToDtoConverter: VoToDtoConverter,
) {
    @Operation(
        summary = "메시지 송신",
        description =
            """
            Websocket을 통한 Stomp 프로토콜을 이용하여 채팅 메시지를 전송합니다.
            Client는 /api/chat/{roomId} 경로로 메시지를 전송합니다.
            """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "메시지 송신 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CommandChatMessageRequestDto::class),
                    ),
                ],
            ),
        ],
    )
    @OpenApiBody(
        content = [
            Content(
                encoding = [
                    Encoding(
                        name = "commandChatMessageVO",
                        contentType = MediaType.APPLICATION_JSON_VALUE,
                    ),
                ],
            ),
        ],
    )
    @MessageMapping("{roomId}")
    fun sendMessage(
        @DestinationVariable roomId: String,
        sendMessageRequestVo: SendMessageRequestVo,
        simpleMessageHeaderAccessor: SimpMessageHeaderAccessor,
    ) {
        // Vo -> Dto
        val commandChatMessageRequestDto = voToDtoConverter.sendMessageRequestVoToDto(sendMessageRequestVo, roomId)

        // messageType이 Enter일 경우에는 처음 등장이므로, Websocket의 세션에 정보 저장 (simpleMessageHeaderAccessor)
        val updateMessageDto =
            when (commandChatMessageRequestDto.messageType) {
                CommandChatMessageType.ENTER -> {
                    simpleMessageHeaderAccessor.sessionAttributes?.apply {
                        put("username", commandChatMessageRequestDto.sender)
                        put("roomId", commandChatMessageRequestDto.roomId)
                    }
                    commandChatMessageRequestDto.copy(message = "${commandChatMessageRequestDto.sender}님이 입장하셨습니다.")
                }
                else -> commandChatMessageRequestDto
            }

        // 메시지 전송
        commandChatMessageService.sendMessage(roomId, updateMessageDto)
    }
}