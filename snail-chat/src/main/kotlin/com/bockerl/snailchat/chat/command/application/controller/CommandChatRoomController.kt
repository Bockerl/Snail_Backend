package com.bockerl.snailchat.chat.command.application.controller

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomCreateRequestDto
import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomDeleteRequestDto
import com.bockerl.snailchat.chat.command.application.mapper.VoToDtoConverter
import com.bockerl.snailchat.chat.command.application.service.CommandChatRoomService
import com.bockerl.snailchat.chat.command.domain.aggregate.vo.request.CommandChatRoomCreateRequestVo
import com.bockerl.snailchat.common.ResponseDto
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
        summary = "채팅방 생성(개인/그룹)",
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
                        schema = Schema(implementation = CommandChatRoomCreateRequestDto::class),
                    ),
                ],
            ),
        ],
    )
    @PostMapping("/create/{memberId}/{memberNickname}/{memberPhoto}")
    fun createChatRoom(
        @RequestBody commandChatRoomCreateRequestVo: CommandChatRoomCreateRequestVo,
        @PathVariable memberId: String, // 향후 Principal을 통해 memberId, memberNickname, memberPhoto
        @PathVariable memberNickname: String,
        @PathVariable memberPhoto: String,
    ): ResponseDto<*> {
        val commandChatRoomCreateRequestDto =
            voToDtoConverter.commandChatRoomCreateRequestVoTODto(
                commandChatRoomCreateRequestVo,
                memberId,
                memberNickname,
                memberPhoto,
            )

        commandChatRoomService.createChatRoom(commandChatRoomCreateRequestDto)

        return ResponseDto.ok(null)
    }

    @Operation(
        summary = "채팅방 삭제 & 나가기",
        description = "사용자가 채팅방을 삭제 or 나가기 한다.(채팅방 정보에서 C",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "채팅방 삭제 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CommandChatRoomCreateRequestDto::class),
                    ),
                ],
            ),
        ],
    )
    @DeleteMapping("/delete/{chatRoomId}/{memberId}/{memberNickname}/{memberPhoto}") // 향후 msa 설계 완료 후 수정할 계획
    fun deleteChatRoom(
        @PathVariable chatRoomId: String,
        @PathVariable memberId: String, // 향후 Principal을 통해 memberId, memberNickname
        @PathVariable memberNickname: String,
        @PathVariable memberPhoto: String,
    ): ResponseDto<*> {
        val commandChatRoomDeleteRequestDto = CommandChatRoomDeleteRequestDto(chatRoomId, memberId, memberNickname, memberPhoto)

        commandChatRoomService.deleteChatRoom(commandChatRoomDeleteRequestDto)

        return ResponseDto.ok(null)
    }
}