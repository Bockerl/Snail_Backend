package com.bockerl.snailmember.member.command.application.controller

import com.bockerl.snailmember.member.command.application.mapper.AuthConverter
import com.bockerl.snailmember.member.command.application.service.AuthService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val authConverter: AuthConverter,
)