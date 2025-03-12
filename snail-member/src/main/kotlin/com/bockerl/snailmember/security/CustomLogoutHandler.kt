package com.bockerl.snailmember.security

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.constraints.Digits
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.LogoutHandler
import org.springframework.stereotype.Component

@Component
class CustomLogoutHandler(
    private val redisTemplate: RedisTemplate<String, String>,
    private val jwtUtil: JwtUtils,
) : LogoutHandler {
    private val logger = KotlinLogging.logger {}

    override fun logout(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authentication: Authentication?,
    ) {
        logger.info { "logout 시작" }
        val validRequest = request as HttpServletRequest
        val accessToken = jwtUtil.extractAccessToken(validRequest)
        try {
            if (accessToken != null) {
                val claims = jwtUtil.parseClaims(accessToken)
                val email = claims.subject

                redisTemplate.execute { connection ->
                    val accessKey = "AT:$email".toByteArray()
                    val refreshKey = "RT:$email".toByteArray()

                    connection.del(accessKey)
                    connection.del(refreshKey)

                    val result = connection.exec()
                    result.size == 2 && result.all { it is Digits }
                }
            }
        } catch (e: Exception) {
            logger.warn { "로그아웃 도중 에러 발생" }
            throw e
        }
    }
}