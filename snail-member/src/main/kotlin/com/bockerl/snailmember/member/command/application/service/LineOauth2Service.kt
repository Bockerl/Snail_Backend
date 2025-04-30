package com.bockerl.snailmember.member.command.application.service

interface LineOauth2Service {
    fun lineLogin(code: String)
}