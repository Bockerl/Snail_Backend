package com.bockerl.snailmember.member.query.vo

import io.swagger.v3.oas.annotations.media.Schema

data class MemberProfileResponseVO(
    @field:Schema(description = "멤버 이메일", example = "snail@gmail.com", type = "String")
    val memberEmail: String,
    @field:Schema(description = "멤버 닉네임", example = "Lin XaoMing", type = "String")
    val memberNickname: String,
    @field:Schema(description = "멤버 프로필 사진", example = "snail.PNG", type = "String")
    val memberPhoto: String,
    @field:Schema(description = "자기 소개", example = "중국인 유학생입니다 :)", type = "String")
    val selfIntroduction: String,
)