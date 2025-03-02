package com.bockerl.snailmember.security

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class CustomAuthenticationEntryPoint : AuthenticationEntryPoint {
    private val log = KotlinLogging.logger {}
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        log.info { "AuthenticationEntryPoint.commence 호출됨" }
        log.info { "예외 클래스: ${authException.javaClass.name}" }
        log.info { "예외 메시지: ${authException.message}" }

        // 원인 예외 출력
        if (authException.cause != null) {
            log.info { "원인 예외 클래스: ${authException.cause!!.javaClass.name}" }
            log.info { "원인 예외 메시지: ${authException.cause!!.message}" }
        }

        val (statusCode, message) = when (authException) {
            // 블랙리스트
            is LockedException -> HttpServletResponse.SC_FORBIDDEN to authException.message
            // 잘못된 아이디 비밀번호
            is BadCredentialsException -> HttpServletResponse.SC_UNAUTHORIZED to "아이디 또는 비밀번호가 일치하지 않습니다."
            // 인증 실패
            else -> HttpServletResponse.SC_BAD_REQUEST to "인증에 실패했습니다."
        }

        // 상태 코드 설정
        response.status = statusCode
        response.contentType = "application/json;charset=UTF-8"

        // json 응답 본문 설정
        val errorResponse = mapOf(
            "timestamp" to LocalDateTime.now().toString(),
            "message" to message,
            "status" to statusCode,
            "path" to request.requestURL,
        )

        val objectMapper = ObjectMapper()
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
