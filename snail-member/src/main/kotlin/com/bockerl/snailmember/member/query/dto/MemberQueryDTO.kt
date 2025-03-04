package com.bockerl.snailmember.member.query.dto

import com.bockerl.snailmember.member.command.domain.aggregate.entity.Gender
import com.bockerl.snailmember.member.command.domain.aggregate.entity.Language
import com.bockerl.snailmember.member.command.domain.aggregate.entity.MemberStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

class MemberQueryDTO(
    @field:Schema(description = "멤버 고유 번호(PK)", example = "MEM-00000001", type = "String")
    val memberId: Long,
    @field:Schema(description = "멤버 이메일", example = "snail@gmail.com", type = "String")
    val memberEmail: String,
    @field:Schema(description = "멤버 비밀번호", example = "snail123", type = "String")
    val memberPassword: String,
    @field:Schema(description = "멤버 닉네임", example = "Lin XaoMing", type = "String")
    val memberNickName: String,
    @field:Schema(description = "멤버 프로필 사진", example = "snail.PNG", type = "String")
    val memberPhoto: String? = null,
    @field:Schema(description = "선호 언어", example = "KOR", type = "String")
    val memberLanguage: Language,
    @field:Schema(description = "계정 생성 시각", example = "2024-12-11 14:45:30", type = "LocalDateTime")
    val createdAt: LocalDateTime,
    @field:Schema(description = "계정 수정 시각", example = "2024-12-11 14:45:30", type = "LocalDateTime")
    val updatedAt: LocalDateTime? = null,
    @field:Schema(description = "멤버 권한", example = "ROLE_USER", type = "String")
    val memberStatus: MemberStatus,
    @field:Schema(description = "멤버 성별", example = "FEMALE", type = "String")
    val memberGender: Gender,
    @field:Schema(description = "국적", example = "CHINA", type = "String")
    val memberRegion: String? = null,
    @field:Schema(description = "멤버 휴대폰 번호", example = "010-0000-0000", type = "String")
    val memberPhoneNumber: String? = null,
    @field:Schema(description = "생년월일", example = "20020227", type = "LocalDate")
    val memberBirth: LocalDate? = null,
    @field:Schema(description = "마지막 접속 시각", example = "2024-12-11 14:45:30", type = "LocalDateTime")
    val lastAccessTime: LocalDateTime,
    @field:Schema(description = "자기 소개", example = "중국인 유학생입니다 :)", type = "String")
    val selfIntroduction: String? = null,
) {
    val formattedId: String
        get() = "MEM-${memberId.toString().padStart(8, '0')}"
}