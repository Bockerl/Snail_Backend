/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.common.exception

class CommonException(
    val errorCode: ErrorCode,
    private val customMessage: String? = null,
    cause: Throwable? = null,
) : RuntimeException(null, cause) {
    override val message: String
        get() = customMessage ?: errorCode.message
}