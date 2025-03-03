/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.common.exception

data class ExceptionDTO(val code: Int, val message: String) {
    companion object {
        fun of(errorCode: ErrorCode): ExceptionDTO = ExceptionDTO(
            code = errorCode.code,
            message = errorCode.message,
        )
    }
}