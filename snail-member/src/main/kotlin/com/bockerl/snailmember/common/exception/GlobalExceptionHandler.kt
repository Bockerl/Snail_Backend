package com.bockerl.snailmember.common.exception

import com.bockerl.snailmember.common.ResponseDTO
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice(basePackages = ["com.bockerl.snail.member"])
class GlobalExceptionHandler {
    // companion object를 사용하여 logger를 정의합니다
    private val log = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(NoHandlerFoundException::class, HttpRequestMethodNotSupportedException::class)
    fun handleNoPageFoundException(e: Exception): ResponseDTO<Nothing> {
        log.error("잘못된 API 엔드포인트 접근: ${e.message}")
        return ResponseDTO.fail(CommonException(ErrorCode.WRONG_ENTRY_POINT))
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleArgumentTypeMismatch(e: MethodArgumentTypeMismatchException): ResponseDTO<Nothing> {
        log.error("메소드 인자 타입 불일치: ${e.message}")
        return ResponseDTO.fail(e)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameter(e: MissingServletRequestParameterException): ResponseDTO<Nothing> {
        log.error("필수 파라미터 누락: ${e.message}")
        return ResponseDTO.fail(e)
    }

    @ExceptionHandler(CommonException::class)
    fun handleCustomException(e: CommonException): ResponseDTO<Nothing> {
        log.error("GlobalExceptionHandle에서 예외 처리 발생: ${e.message}")
        return ResponseDTO.fail(e)
    }

    @ExceptionHandler(Exception::class)
    fun handleServerException(e: Exception): ResponseDTO<Nothing> {
        log.error("서버 내부 오류 발생", e)
        return ResponseDTO.fail(CommonException(ErrorCode.INTERNAL_SERVER_ERROR))
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(e: DataIntegrityViolationException): ResponseDTO<Nothing> {
        log.error("데이터 무결성 위반: ${e.message}")
        return ResponseDTO.fail(CommonException(ErrorCode.DATA_INTEGRITY_VIOLATION))
    }
}