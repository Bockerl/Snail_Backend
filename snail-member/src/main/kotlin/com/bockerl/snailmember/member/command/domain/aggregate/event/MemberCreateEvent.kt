package com.bockerl.snailmember.member.command.domain.aggregate.event

import com.bockerl.snailmember.common.event.BaseMemberEvent
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.Gender
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.Language
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.MemberStatus
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.SignUpPath
import java.time.Instant
import java.time.LocalDate

data class MemberCreateEvent(
    override val memberId: String,
    override val timestamp: Instant = Instant.now(),
    val memberEmail: String,
    val memberPhoneNumber: String,
    val memberNickname: String,
    val memberPhoto: String,
    val memberStatus: MemberStatus,
    val memberLanguage: Language,
    val memberGender: Gender,
    val memberRegion: String,
    val memberBirth: LocalDate,
    val signUpPath: SignUpPath,
) : BaseMemberEvent