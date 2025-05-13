package com.bockerl.snailmember.search.command.domain.aggregate

import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.Gender
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.Language
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.MemberStatus
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.SignUpPath
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import java.time.LocalDate
import java.time.OffsetDateTime

@Document(indexName = "member_document")
data class MemberDocument(
    @Id
    private val memberId: String,
    private val timeStamp: OffsetDateTime,
    private val memberEmail: String,
    val memberPhoneNumber: String,
    val memberNickname: String,
    val memberPhoto: String,
    val memberStatus: MemberStatus,
    val memberLanguage: Language,
    val memberGender: Gender,
    val memberRegion: String,
    val memberBirth: LocalDate,
    val signUpPath: SignUpPath,
)