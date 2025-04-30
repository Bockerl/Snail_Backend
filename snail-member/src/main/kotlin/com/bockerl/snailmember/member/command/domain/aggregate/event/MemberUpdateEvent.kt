package com.bockerl.snailmember.member.command.domain.aggregate.event

import com.bockerl.snailmember.common.event.BaseMemberEvent
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.Gender
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.Language
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.MemberStatus
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

data class MemberUpdateEvent(
    override val memberId: String,
    override val timestamp: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC),
    val memberNickname: String,
    val memberPhoneNumber: String,
    val memberStatus: MemberStatus,
    val memberLanguage: Language,
    val memberGender: Gender,
    val memberRegion: String,
    val memberBirth: LocalDate,
) : BaseMemberEvent