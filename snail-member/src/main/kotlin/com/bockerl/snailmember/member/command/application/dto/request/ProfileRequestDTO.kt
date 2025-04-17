package com.bockerl.snailmember.member.command.application.dto.request

import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.Gender
import java.time.LocalDate

class ProfileRequestDTO(
    val nickName: String,
    val gender: Gender,
    val birth: LocalDate,
    val selfIntroduction: String,
)