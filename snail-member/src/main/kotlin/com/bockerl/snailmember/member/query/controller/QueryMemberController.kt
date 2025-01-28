package com.bockerl.snailmember.member.query.controller

import com.bockerl.snailmember.common.ResponseDTO
import com.bockerl.snailmember.member.command.application.dto.MemberDTO
import com.bockerl.snailmember.member.command.application.mapper.MemberConverter
import com.bockerl.snailmember.member.query.service.QueryMemberService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/member")
class QueryMemberController (
    private val queryMemberService: QueryMemberService,
    private val memberConverter: MemberConverter
) {
    @GetMapping("/{memberId}")
    fun findMemberByMemberId(@PathVariable memberId: Long): ResponseDTO<*> {
        val memberDTO: MemberDTO = queryMemberService.findMemberByMemberId(memberId)
        return ResponseDTO.ok(memberConverter.dtoToResponseVO(memberDTO))
    }
}
