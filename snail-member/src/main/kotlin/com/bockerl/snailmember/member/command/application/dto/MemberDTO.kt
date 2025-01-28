package com.bockerl.snailmember.member.command.application.dto

import com.bockerl.snailmember.member.command.domain.aggregate.entity.Gender
import com.bockerl.snailmember.member.command.domain.aggregate.entity.Language
import com.bockerl.snailmember.member.command.domain.aggregate.entity.MemberStatus
import java.time.LocalDate
import java.time.LocalDateTime

class MemberDTO(
    val memberId: Long? = null,
    val memberEmail: String? = null,
    val memberPassword: String? = null,
    val memberNickName: String? = null,
    val memberPhoto: String? = null,
    val memberLanguage: Language? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val memberStatus: MemberStatus? = null,
    val memberGender: Gender? = null,
    val memberRegion: String? = null,
    val memberPhoneNumber: String? = null,
    val memberBirth: LocalDate? = null,
    val lastAccessTime: LocalDateTime? = null,
    val selfIntroduction: String? = null
)