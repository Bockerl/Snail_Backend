package com.bockerl.snailmember.security.config.event

import java.time.OffsetDateTime
import java.time.ZoneOffset

data class AuthFailEvent(
    val email: String,
    val timeStamp: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC),
    val path: String,
    val ipAddress: String,
    val userAgent: String,
    val failType: String,
    val message: String,
    val cause: String,
)