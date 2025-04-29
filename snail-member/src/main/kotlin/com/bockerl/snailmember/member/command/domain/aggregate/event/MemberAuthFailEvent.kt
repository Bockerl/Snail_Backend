package com.bockerl.snailmember.member.command.domain.aggregate.event

import java.time.Instant

data class MemberAuthFailEvent(
    val email: String,
    val timeStamp: Instant,
    val failType: String,
    val message: String,
    val path: String,
    val cause: String,
)