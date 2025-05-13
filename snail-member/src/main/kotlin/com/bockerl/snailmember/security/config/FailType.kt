package com.bockerl.snailmember.security.config

enum class FailType(
    val code: String,
) {
    BLACK_LIST("BLACK_LIST"),
    LOGIN_FAIL("LOGIN_FAIL"),
    TOKEN_EXPIRED("TOKEN_EXPIRED"),
    BAD_CREDENTIALS("BAD_CREDENTIALS"),
    INSUFFICIENT_AUTH("INSUFFICIENT_AUTH"),
    AUTH_SERVER_ERROR("AUTH_SERVER_ERROR"),
    UNKNOWN("UNKNOWN"),
}