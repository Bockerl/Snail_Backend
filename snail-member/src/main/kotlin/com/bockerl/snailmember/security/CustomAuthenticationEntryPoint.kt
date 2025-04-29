package com.bockerl.snailmember.security

import com.bockerl.snailmember.security.config.FailType
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class CustomAuthenticationEntryPoint(
    private val eventPublisher: ApplicationEventPublisher,
) : AuthenticationEntryPoint {
    private val log = KotlinLogging.logger {}

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        log.info { "AuthenticationEntryPoint.commence 호출됨" }
        log.info { "예외 클래스: ${authException.javaClass.name}" }
        log.info { "예외 메시지: ${authException.message}" }
        val (statusCode, message, failType) =
            when (authException) {
                is LockedException -> Triple(HttpServletResponse.SC_FORBIDDEN, "블랙리스트로 등록된 아이디입니다.", FailType.BLACK_LIST.code)
                is BadCredentialsException -> Triple(HttpServletResponse.SC_FORBIDDEN, "잘못된 인증 정보입니다.", FailType.BAD_CREDENTIALS.code)
                is InsufficientAuthenticationException ->
                    Triple(
                        HttpServletResponse.SC_FORBIDDEN,
                        "만료된 인증 정보입니다.",
                        FailType.TOKEN_EXPIRED.code,
                    )
//                is CredentialsExpiredException -> Triple(HttpServletResponse.SC_FORBIDDEN, "자격 증명이 만료되었습니다.", FailType.CREDENTIALS_EXPIRED.code)
//                is AccountExpiredException -> Triple(HttpServletResponse.SC_FORBIDDEN, "계정 유효기간이 만료되었습니다.", FailType.ACCOUNT_EXPIRED.code)
//                is DisabledException -> Triple(HttpServletResponse.SC_FORBIDDEN, "비활성화된 계정입니다.", FailType.DISABLED_ACCOUNT.code)
                is AuthenticationServiceException ->
                    Triple(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "인증 서비스에 오류가 발생했습니다.",
                        FailType.AUTH_SERVER_ERROR.code,
                    )
                else -> Triple(HttpServletResponse.SC_UNAUTHORIZED, "인증되지 않은 요청입니다.", FailType.UNKNOWN.code)
            }
        // 상태 코드 설정
        response.status = statusCode
        response.contentType = "application/json;charset=UTF-8"
        // 인증 실패, 이벤트 생성
//        val event =
        // json 응답 본문 설정
        val errorResponse =
            mapOf(
                "timestamp" to LocalDateTime.now().toString(),
                "message" to message,
                "status" to statusCode,
                "path" to request.requestURL,
                "cause" to authException.cause?.javaClass?.name,
            )
        val objectMapper = ObjectMapper()
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}