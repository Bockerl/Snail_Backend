package com.bockerl.snailmember.member.command.application.service

interface GoogleOauth2Service {
    fun googleLogin(code: String): String
}
