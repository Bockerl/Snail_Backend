package com.bockerl.snailmember.member.command.domain.vo.request

import com.bockerl.snailmember.member.command.domain.aggregate.entity.Gender
import java.time.LocalDate

data class ProfileRequestVO(
    val nickName: String?,
    val gender: Gender?,
    val birth: LocalDate?,
    val selfIntroduction: String?,
)