@file:Suppress("ktlint:standard:import-ordering", "ktlint:standard:no-wildcard-imports")

package com.bockerl.snailchat.chat.command.application.controller

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDTO
import com.bockerl.snailchat.chat.command.application.mapper.VoToDtoConverter
import com.bockerl.snailchat.chat.command.application.service.CommandChatMessageService
import com.bockerl.snailchat.chat.command.domain.aggregate.enums.ChatMessageType
import com.bockerl.snailchat.chat.command.domain.aggregate.vo.request.SendMessageRequestVo
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor

@RestController
@RequestMapping("/api/chatMessage")
class CommandChatMessageController(
    private val commandChatMessageService: CommandChatMessageService,
    private val voToDtoConverter: VoToDtoConverter,
) {
    //    @MessageMapping("{chatRoomId}")
    @Operation(
        summary = "메시지 송신 (Stomp) ",
        description =
            """
            Websocket을 통한 Stomp 프로토콜을 이용하여 채팅 메시지를 전송합니다.
            Client는 /topic/{chatRoomId} 경로로 메시지를 전송합니다.
            """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "메시지 송신(Stomp) 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CommandChatMessageRequestDTO::class),
                    ),
                ],
            ),
        ],
    )
    fun sendMessage(
        @DestinationVariable chatRoomId: String,
        sendMessageRequestVo: SendMessageRequestVo,
        simpleMessageHeaderAccessor: SimpMessageHeaderAccessor,
    ) {
        // Vo -> Dto + 토큰에서 memberId/Nickname/Photo를 받아올 수 있도록 수정해야 함
        val commandChatMessageRequestDto = voToDtoConverter.sendMessageRequestVoToDto(sendMessageRequestVo, chatRoomId)

        // messageType이 Enter일 경우에는 처음 등장이므로, Websocket의 세션에 정보 저장 (simpleMessageHeaderAccessor)
        val updateMessageDto =
            when (commandChatMessageRequestDto.messageType) {
                ChatMessageType.ENTER -> {
                    simpleMessageHeaderAccessor.sessionAttributes?.apply {
                        // 향후 Token 완성 후 수정필요
                        put("memberId", commandChatMessageRequestDto.memberId)
                        put("memberNickname", commandChatMessageRequestDto.memberNickname)
                        put("memberPhoto", commandChatMessageRequestDto.memberPhoto)
                        put("chatRoomId", commandChatMessageRequestDto.chatRoomId)
                    }

                    commandChatMessageRequestDto.copy(message = "${commandChatMessageRequestDto.memberNickname}님이 입장하셨습니다.")
                }
                else -> commandChatMessageRequestDto
            }

        // 메시지 전송
        commandChatMessageService.sendMessage(updateMessageDto)
    }

    @Operation(
        summary = "메시지 송신 (Kafka) ",
        description =
            """
            Websocket을 통한 Kafka 프로토콜을 이용하여 채팅 메시지를 전송합니다.
            Client는 /topic/{chatRoomId} 경로로 메시지를 전송합니다.
            """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "메시지 송신(Kafka) 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CommandChatMessageRequestDTO::class),
                    ),
                ],
            ),
        ],
    )
    fun sendMessageByKafka(
        @DestinationVariable chatRoomId: String,
        sendMessageRequestVo: SendMessageRequestVo,
        simpleMessageHeaderAccessor: SimpMessageHeaderAccessor,
    ) {
        // Vo -> Dto + 토큰에서 memberId/Nickname/Photo를 받아올 수 있도록 수정해야 함
        val commandChatMessageRequestDTO = voToDtoConverter.sendMessageRequestVoToDto(sendMessageRequestVo, chatRoomId)

        // messageType이 Enter일 경우에는 처음 등장이므로, Websocket의 세션에 정보 저장 (simpleMessageHeaderAccessor)
        val updateMessageDTO =
            when (commandChatMessageRequestDTO.messageType) {
                ChatMessageType.ENTER -> {
                    simpleMessageHeaderAccessor.sessionAttributes?.apply {
                        // 향후 Token 완성 후 수정필요
                        put("memberId", commandChatMessageRequestDTO.memberId)
                        put("memberNickname", commandChatMessageRequestDTO.memberNickname)
                        put("memberPhoto", commandChatMessageRequestDTO.memberPhoto)
                        put("chatRoomId", commandChatMessageRequestDTO.chatRoomId)
                    }

                    commandChatMessageRequestDTO.copy(message = "${commandChatMessageRequestDTO.memberNickname}님이 입장하셨습니다.")
                }
                else -> commandChatMessageRequestDTO
            }

        // 메시지 전송
        commandChatMessageService.sendMessageByKafka(updateMessageDTO)
    }

    @Operation(
        summary = "메시지 송신 (Kafka + Outbox) ",
        description =
            """
            Websocket을 통한 Kafka 프로토콜을 이용하여 채팅 메시지를 전송합니다.
            Outbox 패턴을 사용해서 메시지 유실을 방지합니다.
            Client는 /topic/{chatRoomId} 경로로 메시지를 전송합니다.
            """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "메시지 송신(Kafka + Outbox) 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CommandChatMessageRequestDTO::class),
                    ),
                ],
            ),
        ],
    )
    @MessageMapping("{chatRoomId}")
    fun sendMessageByKafkaOutbox(
        @DestinationVariable chatRoomId: String,
        @Header("idempotencyKey") idempotencyKey: String,
        @Payload sendMessageRequestVo: SendMessageRequestVo,
        simpleMessageHeaderAccessor: SimpMessageHeaderAccessor,
    ) {
        println("idempotencyKey:$idempotencyKey")
        // Vo -> Dto + 토큰에서 memberId/Nickname/Photo를 받아올 수 있도록 수정해야 함
        val commandChatMessageKeyRequestDTO =
            voToDtoConverter.sendMessageRequestVoAndKeyToDto(
                sendMessageRequestVo,
                chatRoomId,
                idempotencyKey,
            )

        // messageType이 Enter일 경우에는 처음 등장이므로, Websocket의 세션에 정보 저장 (simpleMessageHeaderAccessor)
        val updateMessageDTO =
            when (commandChatMessageKeyRequestDTO.messageType) {
                ChatMessageType.ENTER -> {
                    simpleMessageHeaderAccessor.sessionAttributes?.apply {
                        // 향후 Token 완성 후 수정필요
                        put("memberId", commandChatMessageKeyRequestDTO.memberId)
                        put("memberNickname", commandChatMessageKeyRequestDTO.memberNickname)
                        put("memberPhoto", commandChatMessageKeyRequestDTO.memberPhoto)
                        put("chatRoomId", commandChatMessageKeyRequestDTO.chatRoomId)
                    }

                    commandChatMessageKeyRequestDTO.copy(message = "${commandChatMessageKeyRequestDTO.memberNickname}님이 입장하셨습니다.")
                }
                else -> commandChatMessageKeyRequestDTO
            }

        // 메시지 전송
        commandChatMessageService.sendMessageByKafkaOutbox(updateMessageDTO)
    }
}