package com.bockerl.snailchat.common.exception

data class ExceptionDto(
    val code: Int,
    val message: String,
) {
    companion object {
        fun of(errorCode: ErrorCode): ExceptionDto =
            ExceptionDto(
                code = errorCode.code,
                message = errorCode.message,
            )
    }
}