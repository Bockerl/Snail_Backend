package com.bockerl.snailmember.member.command.application.dto.request

class EmailVerifyRequestDTO(val verificationCode: String, val redisId: String)
