@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailchat.chat.query.controller

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomCreateRequestDto
import com.bockerl.snailchat.chat.query.dto.request.QueryGroupChatRoomRequestDto
import com.bockerl.snailchat.chat.query.dto.request.QueryPersonalChatRoomRequestDto
import com.bockerl.snailchat.chat.query.service.QueryChatRoomService
import com.bockerl.snailchat.common.ResponseDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chatRoom")
class QueryChatRoomController(
    private val queryChatRoomService: QueryChatRoomService,
) {
    @Operation(
        summary = "채팅방 전체 조회(개인)",
        description = "사용자가 본인이 참여하고 있는 개인 채팅방을 전부 조회",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "개인 채팅방 조회 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CommandChatRoomCreateRequestDto::class),
                    ),
                ],
            ),
        ],
    )
    @GetMapping("/personal/{memberId}")
    fun getPersonalChatRoomListByMemberId(
        @PathVariable memberId: String,
        @RequestParam(required = false) lastId: String? = null,
        @RequestParam(defaultValue = "10") pageSize: Int,
    ): ResponseDto<*> {
        val queryPersonalChatRoomRequestDto = QueryPersonalChatRoomRequestDto(memberId, lastId, pageSize)

        val personalChatRoomList = queryChatRoomService.getPersonalChatRoomList(queryPersonalChatRoomRequestDto)

        return ResponseDto.ok(personalChatRoomList)
    }

    @Operation(
        summary = "채팅방 전체 조회(그룹)",
        description = "사용자가 본인이 참여하고 있는 그룹 채팅방을 전부 조회",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "그룹 채팅방 조회 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CommandChatRoomCreateRequestDto::class),
                    ),
                ],
            ),
        ],
    )
    @GetMapping("/group/{memberId}")
    fun getGroupChatRoomListByMemberId(
        @PathVariable memberId: String,
        @RequestParam(required = false) lastId: String? = null,
        @RequestParam(defaultValue = "10") pageSize: Int,
    ): ResponseDto<*> {
        val queryGroupChatRoomRequestDto = QueryGroupChatRoomRequestDto(memberId, lastId, pageSize)

        val groupChatRoomList = queryChatRoomService.getGroupChatRoomList(queryGroupChatRoomRequestDto)

        return ResponseDto.ok(groupChatRoomList)
    }
}