package com.bockerl.snailchat.common

import com.bockerl.snailchat.common.exception.CommonException
import com.bockerl.snailchat.common.exception.ErrorCode
import com.bockerl.snailchat.common.exception.ExceptionDto
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.lang.Nullable
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@Validated
data class ResponseDto<T>(
    @field:JsonIgnore
    val httpStatus: HttpStatus? = null,
    @field:NotNull
    val success: Boolean = false,
    @field:Nullable
    val data: T? = null,
    @field:Nullable
    val error: ExceptionDto? = null,
) {
    companion object {
        // 성공 응답 생성
        fun <T> ok(data: T?): ResponseDto<T> =
            ResponseDto(
                httpStatus = HttpStatus.OK,
                success = true,
                data = data,
                error = null,
            )

        // 커스텀 예외 기반 실패 응답
        fun fail(e: CommonException): ResponseDto<Nothing> =
            ResponseDto(
                httpStatus = e.errorCode.httpStatus,
                success = false,
                data = null,
                error = ExceptionDto.of(e.errorCode),
            )

        // 파라미터 누락 예외 처리
        fun fail(e: MissingServletRequestParameterException): ResponseDto<Nothing> =
            ResponseDto(
                httpStatus = HttpStatus.BAD_REQUEST,
                success = false,
                data = null,
                error = ExceptionDto.of(ErrorCode.MISSING_REQUEST_PARAMETER),
            )

        // 타입 불일치 예외 처리
        fun fail(e: MethodArgumentTypeMismatchException): ResponseDto<Nothing> =
            ResponseDto(
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
                success = false,
                data = null,
                error = ExceptionDto.of(ErrorCode.INVALID_PARAMETER_FORMAT),
            )
    }
}