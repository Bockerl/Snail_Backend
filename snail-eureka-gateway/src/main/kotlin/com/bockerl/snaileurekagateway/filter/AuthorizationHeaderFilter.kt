package com.bockerl.snaileurekagateway.filter

import io.github.oshai.kotlinlogging.KotlinLogging
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
import java.util.Date

@Component
class AuthorizationHeaderFilter(
    private val env: Environment,
) : AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config>(Config::class.java) {
    private val logger = KotlinLogging.logger {}

    class Config

    override fun apply(config: Config?): GatewayFilter =
        GatewayFilter { exchange, chain ->
            val request = exchange.request

            // AT 확인
            if (!request.headers.containsKey(HttpHeaders.AUTHORIZATION)) {
                logger.info { "인증 헤더가 없습니다. 인증 서비스로 리디렉션합니다." }
                return@GatewayFilter redirectToAuthService(exchange)
            }

            val bearerToken = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
            val jwt = bearerToken?.replace("Bearer ", "")

            if (jwt.isNullOrBlank() || !isValid(jwt)) {
                logger.info { "JWT 토큰이 유효하지 않습니다. 인증 서비스로 리디렉션합니다." }
                return@GatewayFilter redirectToAuthService(exchange)
            }

            return@GatewayFilter chain.filter(exchange)
        }

    private fun redirectToAuthService(exchange: ServerWebExchange): Mono<Void>? {
        val response = exchange.response
        response.statusCode = HttpStatus.TEMPORARY_REDIRECT

        // 현재 요청 URI를 쿼리 파라미터로 전달하여 로그인 후 원래 페이지로 돌아올 수 있게 함
        val redirectUrl = "/api/member/login?redirect=${exchange.request.uri}"
        response.headers.location = URI.create(redirectUrl)

        return response.setComplete()
    }

    private fun isValid(jwt: String?): Boolean {
        return try {
            // ConfigMap 또는 Secret에서 토큰 secret 가져올 예정
            val secret = env.getProperty("token.secret") ?: throw Exception("토큰 secret 설정 실패")

            val claims =
                Jwts
                    .parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secret.toByteArray()))
                    .build()
                    .parseClaimsJws(jwt)
                    .body

            // 만료 시간 검증 추가
            if (claims.expiration != null && claims.expiration.before(Date())) {
                logger.warn { "토큰이 만료되었습니다." }
                return false
            }
            true
        } catch (e: Exception) {
            logger.warn { "Jwt 검증 오류: $e" }
            false
        }
    }
}