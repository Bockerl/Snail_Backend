package com.bockerl.snailmember.member.command.application.service

interface KaKaoOauth2Service {
    fun kakaoLogin(code: String)
}