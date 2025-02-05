package com.bockerl.snailmember.member.command.application.controller

import com.bockerl.snailmember.member.command.application.mapper.MemberConverter
import com.bockerl.snailmember.member.command.application.service.CommandMemberService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/member")
class CommandMemberController(
    private val commandMemberService: CommandMemberService,
    private val memberConverter: MemberConverter,
) {
//    @PostMapping("//")
}