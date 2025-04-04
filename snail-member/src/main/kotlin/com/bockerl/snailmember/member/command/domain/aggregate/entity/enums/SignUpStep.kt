package com.bockerl.snailmember.member.command.domain.aggregate.entity.tempMember

enum class SignUpStep {
    INITIAL,
    EMAIL_VERIFIED,
    PHONE_VERIFIED,
    PASSWORD_VERIFIED,
}