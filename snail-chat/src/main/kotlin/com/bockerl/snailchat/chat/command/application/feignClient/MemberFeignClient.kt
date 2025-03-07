package com.bockerl.snailchat.chat.command.application.feignClient

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

// @FeignClient(name = "member-service", url = "\${member.service.url}")
interface MemberFeignClient {
    @GetMapping("/api/members/{memberId}")
    fun getCurrentMemberInfo(
        @PathVariable memberId: String,
    ): MemberInfoResponse

    @GetMapping("api/boards/{boardId}")
    fun getBoardMemberInfo(
        @PathVariable memberId: String,
    ): MemberInfoResponse
}

data class MemberInfoResponse(
    val memberId: String,
    val memberNickname: String,
    val memberPhoto: String? = null,
)