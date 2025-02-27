package com.bockerl.snailmember.member.command.application.service

interface CommandMemberService {
    fun putLastAccessTime(memberEmail: String)
}
