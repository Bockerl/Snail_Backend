package com.bockerl.snailmember.common.exception

class CommonException(
    val errorCode: ErrorCode,
    cause: Throwable? = null // Exception의 원인이 들어가는 프로퍼티
) : RuntimeException(null, cause) {
    override val message: String
        get() = errorCode.message
}