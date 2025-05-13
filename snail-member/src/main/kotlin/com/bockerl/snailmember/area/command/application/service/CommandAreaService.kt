package com.bockerl.snailmember.area.command.application.service

import com.bockerl.snailmember.member.command.application.dto.request.ActivityAreaRequestDTO

interface CommandAreaService {
    fun postActivityArea(
        memberId: String,
        requestDTO: ActivityAreaRequestDTO,
        idempotencyKey: String,
    )

    fun deleteActivityArea(
        memberId: String,
        idempotencyKey: String,
    )
}