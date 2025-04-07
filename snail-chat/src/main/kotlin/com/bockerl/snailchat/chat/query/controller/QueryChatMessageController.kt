@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailchat.chat.query.controller

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDTO
import com.bockerl.snailchat.chat.query.dto.request.chatMessageDTO.QueryChatMessageRequestDTO
import com.bockerl.snailchat.chat.query.dto.request.chatMessageDTO.QuerySearchChatMessageRequestDTO
import com.bockerl.snailchat.chat.query.service.QueryChatMessageService
import com.bockerl.snailchat.common.ResponseDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chatMessage")
class QueryChatMessageController(
    private val queryChatMessageService: QueryChatMessageService,
) {
    @Operation(
        summary = "채팅방 메시지 조회",
        description = "사용자가 클릭한 채팅방 메시지 조회",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "선택한 채팅방 메시지 조회 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CommandChatMessageRequestDTO::class),
                    ),
                ],
            ),
        ],
    )
    @GetMapping("/{chatRoomId}")
    fun getChatMessageByChatRoomId(
        @PathVariable chatRoomId: String,
        @RequestParam(required = false) lastId: String? = null,
        @RequestParam(defaultValue = "10") pageSize: Int,
    ): ResponseDTO<*> {
        val queryChatMessageRequestDTO = QueryChatMessageRequestDTO(chatRoomId, lastId, pageSize)

        val chatMessageList = queryChatMessageService.getChatMessageByChatRoomId(queryChatMessageRequestDTO)

        return ResponseDTO.ok(chatMessageList)
    }

    @Operation(
        summary = "채팅방 입장시 여부 출력 (최초, 재입장)",
        description = "사용자가 채팅방 입장시 최초 or 재입장 여부 출력 ",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "채팅방 입장 여부 출력 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CommandChatMessageRequestDTO::class),
                    ),
                ],
            ),
        ],
    )
    @GetMapping("/{chatRoomId}/{memberId}")
    fun isFirstJoin(
        @PathVariable chatRoomId: String,
        @PathVariable memberId: String,
    ): ResponseDTO<*> {
        val isFirstJoinStatus = queryChatMessageService.getIsFirstJoin(chatRoomId, memberId)

        return ResponseDTO.ok(isFirstJoinStatus)
    }

    @Operation(
        summary = "채팅 메시지 검색",
        description = "선택한 채팅방 메시지 키워드로 검색",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "채팅 검색 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CommandChatMessageRequestDTO::class),
                    ),
                ],
            ),
        ],
    )
    @GetMapping("/search/{chatRoomId}")
    fun searchChatMessageByKeyword(
        @PathVariable chatRoomId: String,
        @RequestParam(required = false) keyword: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") pageSize: Int,
    ): ResponseDTO<*> {
        val querySearchChatMessageRequestDTO = QuerySearchChatMessageRequestDTO(chatRoomId, keyword, pageSize, page)
        val searchResults = queryChatMessageService.searchChatMessageByKeyword(querySearchChatMessageRequestDTO)

        return ResponseDTO.ok(searchResults)
    }
}