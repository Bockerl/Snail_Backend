/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.member.command.domain.aggregate.vo.response

import com.bockerl.snailmember.member.command.domain.aggregate.entity.Gender
import com.bockerl.snailmember.member.command.domain.aggregate.entity.Language
import com.bockerl.snailmember.member.command.domain.aggregate.entity.MemberStatus
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

data class MemberResponseVO(
    @field:Schema(description = "멤버 고유 번호(PK)", example = "1", type = "Long")
    @JsonProperty(namespace = "memberId")
    val memberId: String? = null,
    @field:Schema(description = "멤버 이메일", example = "snail@gmail.com", type = "String")
    @JsonProperty(namespace = "memberEmail")
    val memberEmail: String? = null,
    @field:Schema(description = "멤버 비밀번호", example = "snail123", type = "String")
    @JsonProperty(namespace = "memberPassword")
    val memberPassword: String? = null,
    @field:Schema(description = "멤버 닉네임", example = "Lin XaoMing", type = "String")
    @JsonProperty(namespace = "memberNickName")
    val memberNickName: String? = null,
    @field:Schema(description = "멤버 프로필 사진", example = "snail.PNG", type = "String")
    @JsonProperty(namespace = "memberPhoto")
    val memberPhoto: String? = null,
    @field:Schema(description = "선호 언어", example = "KOR", type = "String")
    @JsonProperty(namespace = "memberLanguage")
    val memberLanguage: Language? = null,
    @field:Schema(description = "계정 생성 시각", example = "2024-12-11 14:45:30", type = "LocalDateTime")
    @JsonProperty(namespace = "createdAt")
    val createdAt: LocalDateTime? = null,
    @field:Schema(description = "계정 수정 시각", example = "2024-12-11 14:45:30", type = "LocalDateTime")
    @JsonProperty(namespace = "updatedAt")
    val updatedAt: LocalDateTime? = null,
    @field:Schema(description = "멤버 권한", example = "ROLE_USER", type = "String")
    @JsonProperty(namespace = "memberStatus")
    val memberStatus: MemberStatus? = null,
    @field:Schema(description = "멤버 성별", example = "FEMALE", type = "String")
    @JsonProperty(namespace = "memberGender")
    val memberGender: Gender? = null,
    @field:Schema(description = "국적", example = "CHINA", type = "String")
    @JsonProperty(namespace = "memberRegion")
    val memberRegion: String? = null,
    @field:Schema(description = "멤버 휴대폰 번호", example = "010-0000-0000", type = "String")
    @JsonProperty(namespace = "memberPhoneNumber")
    val memberPhoneNumber: String? = null,
    @field:Schema(description = "생년월일", example = "2002-02-27", type = "LocalDate")
    @JsonProperty(namespace = "memberBirth")
    val memberBirth: LocalDate? = null,
    @field:Schema(description = "마지막 접속 시각", example = "2024-12-11 14:45:30", type = "LocalDateTime")
    val lastAccessTime: LocalDateTime? = null,
    @field:Schema(description = "자기 소개", example = "중국인 유학생입니다 :)", type = "String")
    @JsonProperty(namespace = "selfIntroduction")
    val selfIntroduction: String? = null,
)