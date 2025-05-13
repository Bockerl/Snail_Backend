package com.bockerl.snailmember.security

import com.bockerl.snailmember.security.config.FailType
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class CustomAuthenticationFailureHandler(
    private val eventPublisher: ApplicationEventPublisher,
) : AuthenticationFailureHandler {
    private val log = KotlinLogging.logger {}

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException,
    ) {
        log.info { "onAuthenticationFailure 호출됨" }
        log.info { exception }
        log.info { "예외 클래스: ${exception.javaClass.name}" }
        log.info { "예외 메시지: ${exception.message}" }

        val (statusCode, message, failType) =
            when (exception) {
                // 블랙리스트
                is LockedException -> Triple(HttpServletResponse.SC_FORBIDDEN, "블랙리스트로 등록된 아이디입니다.", FailType.BLACK_LIST.code)
                // 잘못된 아이디 비밀번호
                is BadCredentialsException -> Triple(HttpServletResponse.SC_UNAUTHORIZED, "잘못된 아이디 혹은 비밀번호입니다.", FailType.LOGIN_FAIL.code)
                // 토큰 만료
                is InsufficientAuthenticationException ->
                    Triple(
                        HttpServletResponse.SC_FORBIDDEN,
                        "토큰이 만료되었습니다.",
                        FailType.TOKEN_EXPIRED.code,
                    )
                // 인증 실패
                else -> Triple(HttpServletResponse.SC_BAD_REQUEST, "인증에 실패했습니다.", FailType.UNKNOWN.code)
            }

        // 상태 코드 설정
        response.status = statusCode
        response.contentType = "application/json;charset=UTF-8"

        // json 응답 본문 설정
        val errorResponse =
            mapOf(
                "timestamp" to LocalDateTime.now().toString(),
                "message" to message,
                "status" to statusCode,
                "path" to request.requestURL,
            )

        val objectMapper = ObjectMapper()
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}