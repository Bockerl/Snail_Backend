/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */

package com.bockerl.snailmember.common

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.common.exception.ExceptionDTO
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.lang.Nullable
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@Validated
data class ResponseDTO<T>(
    @field:JsonIgnore
    val httpStatus: HttpStatus? = null,
    @field:NotNull
    val success: Boolean = false,
    @field:Nullable
    val data: T? = null,
    @field:Nullable
    val error: ExceptionDTO? = null,
) {
    companion object {
        // 성공 응답 생성
        fun <T> ok(data: T?): ResponseDTO<T> = ResponseDTO(
            httpStatus = HttpStatus.OK,
            success = true,
            data = data,
            error = null,
        )

        // 커스텀 예외 기반 실패 응답
        fun fail(e: CommonException): ResponseDTO<Nothing> = ResponseDTO(
            httpStatus = e.errorCode.httpStatus,
            success = false,
            data = null,
            error = ExceptionDTO.of(e.errorCode),
        )

        // 파라미터 누락 예외 처리
        fun fail(e: MissingServletRequestParameterException): ResponseDTO<Nothing> = ResponseDTO(
            httpStatus = HttpStatus.BAD_REQUEST,
            success = false,
            data = null,
            error = ExceptionDTO.of(ErrorCode.MISSING_REQUEST_PARAMETER),
        )

        // 타입 불일치 예외 처리
        fun fail(e: MethodArgumentTypeMismatchException): ResponseDTO<Nothing> = ResponseDTO(
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
            success = false,
            data = null,
            error = ExceptionDTO.of(ErrorCode.INVALID_PARAMETER_FORMAT),
        )
    }
}