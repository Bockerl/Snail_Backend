package com.bockerl.snaileurekagateway.filter

import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.core.env.Environment
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.lang.Exception
import java.net.URI

@Component
class CheckRoleFilter(
    private val env: Environment,
) : AbstractGatewayFilterFactory<CheckRoleFilter.Config>(Config::class.java) {
    private val logger = KotlinLogging.logger {}

    data class Config(
        var roleList: String = "",
    )

    override fun apply(config: Config): GatewayFilter =
        GatewayFilter { exchange, chain ->
            val request = exchange.request
            val headers = request.headers

            if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
                logger.warn { "인증 헤더가 없습니다. 인증 서비스로 리디렉션합니다." }
                return@GatewayFilter redirectToAuthService(exchange)
            }

            val bearerToken = headers.getFirst(HttpHeaders.AUTHORIZATION)
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                logger.warn { "유효하지 않은 인증 토큰 형식입니다. 인증 서비스로 리디렉션합니다." }
                return@GatewayFilter redirectToAuthService(exchange)
            }

            val jwt = bearerToken.substring(7)

            try {
                val secret = env.getProperty("token.secret") ?: throw Exception("토큰 secret 설정 실패")
                val claims =
                    Jwts
                        .parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(secret.toByteArray()))
                        .build()
                        .parseClaimsJws(jwt)
                        .body
                val role = claims["auth"]?.toString()
                if (role.isNullOrBlank()) {
                    logger.warn { "토큰에 역할 정보가 없습니다. 인증 서비스로 리디렉션합니다." }
                    return@GatewayFilter redirectToAuthService(exchange)
                }

                // 쉼표로 구분된 역할 목록을 파싱
                val requiredRoles = config.roleList.split(",").map { it.trim() }
                if (requiredRoles.isNotEmpty() && role !in requiredRoles) {
                    logger.warn { "사용자 역할($role)이 필요 역할($requiredRoles) 중 하나와 일치하지 않습니다. 인증 서비스로 리디렉션합니다." }
                    return@GatewayFilter redirectToAuthService(exchange)
                }

                return@GatewayFilter chain.filter(exchange)
            } catch (e: ExpiredJwtException) {
                logger.warn { "JWT이 만료되었습니다. 인증 서비스로 리디렉션합니다." }
                return@GatewayFilter redirectToAuthService(exchange)
            }
        }

    private fun redirectToAuthService(exchange: ServerWebExchange): Mono<Void>? {
        val response = exchange.response
        response.statusCode = HttpStatus.TEMPORARY_REDIRECT

        // 현재 요청 URI를 쿼리 파라미터로 전달하여 로그인 후 원래 페이지로 돌아올 수 있게 함
        val redirectUrl = "/api/member/login?redirect=${exchange.request.uri}"
        response.headers.location = URI.create(redirectUrl)

        return response.setComplete()
    }
}