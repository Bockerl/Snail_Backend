/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.common.exception

class CommonException(
    val errorCode: ErrorCode,
    cause: Throwable? = null,
) : RuntimeException(null, cause) {
    override val message: String
        get() = errorCode.message
}