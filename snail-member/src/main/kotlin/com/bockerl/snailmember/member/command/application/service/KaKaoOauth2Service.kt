package com.bockerl.snailmember.member.command.application.service

interface Oauth2Service {
    fun getKaKaoToken(code: String): String
}
