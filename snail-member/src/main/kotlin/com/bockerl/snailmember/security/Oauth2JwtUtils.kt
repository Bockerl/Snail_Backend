package com.bockerl.snailmember.security

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.command.application.service.CommandMemberService
import com.bockerl.snailmember.security.config.TokenType
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.core.env.Environment
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.lang.Exception
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import javax.crypto.spec.SecretKeySpec

@Component
class Oauth2JwtUtils(
    environment: Environment,
    private val redisTemplate: RedisTemplate<String, String>,
    private val commandMemberService: CommandMemberService,
) {
    companion object {
        private const val REDIS_RT_PREFIX = "RT:"
        private const val REDIS_AT_PREFIX = "AT:"
    }

    private val logger = KotlinLogging.logger {}

    // 환경 변수 관리
    private val accessExpiration =
        environment.getProperty("ACCESS_TOKEN_EXPIRATION")?.toLong()
            ?: throw CommonException(ErrorCode.NOT_FOUND_ENV)
    private val refreshExpiration =
        environment.getProperty("REFRESH_TOKEN_EXPIRATION")?.toLong()
            ?: throw CommonException(ErrorCode.NOT_FOUND_ENV)
    private val tokenIssuer =
        environment.getProperty("TOKEN_ISSUER")
            ?: throw CommonException(ErrorCode.NOT_FOUND_ENV)
    private val tokenSecret =
        environment.getProperty("TOKEN_SECRET")
            ?: throw CommonException(ErrorCode.NOT_FOUND_ENV)

    fun generateJwtResponse(customMember: CustomMember) {
        val email = customMember.memberEmail
        // accessToken 생성
        val accessToken =
            runCatching { generateAccessToken(customMember) }
                .getOrElse {
                    logger.error { "accessToken 처리 중 에러 발생, memberId: ${customMember.memberId}" }
                    throw CommonException(ErrorCode.TOKEN_GENERATION_ERROR)
                }
        // refreshToken 생성
        val refreshToken =
            runCatching { getOrCreateRefreshToken(email, customMember.authorities.firstOrNull()?.authority) }
                .getOrElse {
                    logger.error { "refreshToken 처리 중 에러 발생, memberId: ${customMember.memberId}" }
                    throw CommonException(ErrorCode.TOKEN_GENERATION_ERROR)
                }
        // Header에 담기위한 response 꺼내기
        val reqAttrs = RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes
        val response = reqAttrs.response
        response?.setHeader("Authorization", "Bearer $accessToken")
        response?.setHeader("refreshToken", refreshToken)
        // ipAddress, userAgent, idempotencyKey 추출
        val httpRequest = reqAttrs.request
        val ipAddress = httpRequest.remoteAddr
        val userAgent = httpRequest.getHeader("User-Agent")
        val idempotencyKey = "$email:${Instant.now().truncatedTo(ChronoUnit.SECONDS)}"
        commandMemberService.putLastAccessTime(email, ipAddress, userAgent, idempotencyKey)
    }

    private fun generateAccessToken(customMember: CustomMember): String {
        logger.info { "AccessToken 생성 시작" }
        val email = customMember.memberEmail
        // 기존 AccessToken 삭제
        deleteAccessToken(email)
        // AccessToken Claims 생성
        val claims =
            Jwts.claims().apply {
                subject = customMember.memberEmail
            }
        val authority = customMember.authorities.firstOrNull()?.authority
        claims["auth"] = authority
        claims["memberNickname"] = customMember.memberNickname
        claims["memberId"] = customMember.memberId
        claims["memberPhoto"] = customMember.memberPhoto
        // AccessToken 생성
        val accessTokenExpiration = System.currentTimeMillis() + accessExpiration * 1000
        val accessToken = generateToken(claims, accessTokenExpiration)
        // Redis에 저장
        saveToken(email, accessToken, TokenType.ACCESS_TOKEN)
        logger.info { "AccessToken 생성 완료: $accessToken" }
        return accessToken
    }

    private fun getOrCreateRefreshToken(
        email: String,
        authority: String?,
    ): String {
        logger.info { "RefreshToken 처리 시작" }
        // Redis에서 기존 RefreshToken 조회
        val existingRefreshToken = redisTemplate.opsForValue().get("$REDIS_RT_PREFIX$email")
        // 기존 토큰이 있으면 재사용, 없으면 새로 생성
        return if (existingRefreshToken != null) {
            logger.info { "기존 RefreshToken 재사용" }
            existingRefreshToken
        } else {
            logger.info { "새 RefreshToken 생성" }
            val refreshClaims =
                Jwts.claims().apply {
                    subject = email
                    this["auth"] = authority
                }
            val refreshTokenExpiration = System.currentTimeMillis() + refreshExpiration * 1000
            val refreshToken = generateToken(refreshClaims, refreshTokenExpiration)
            saveToken(email, refreshToken, TokenType.REFRESH_TOKEN)
            logger.info { "새 refreshToken 생성 완료: $refreshToken" }
            return refreshToken
        }
    }

    private fun generateToken(
        claims: Claims?,
        expiration: Long,
    ): String {
        val token: String =
            Jwts
                .builder()
                .setClaims(claims)
                .setIssuedAt(Date())
                .setExpiration(Date(expiration))
                .setIssuer(tokenIssuer)
                .signWith(SecretKeySpec(tokenSecret.toByteArray(), SignatureAlgorithm.HS512.jcaName))
                .compact()!!
        return token
    }

    private fun saveToken(
        email: String,
        token: String,
        type: TokenType,
    ) {
        redisTemplate.execute { connection ->
            val key =
                when (type) {
                    TokenType.ACCESS_TOKEN -> "${REDIS_AT_PREFIX}$email".toByteArray()
                    TokenType.REFRESH_TOKEN -> "${REDIS_RT_PREFIX}$email".toByteArray()
                }
            val expiration =
                when (type) {
                    TokenType.ACCESS_TOKEN -> accessExpiration
                    TokenType.REFRESH_TOKEN -> refreshExpiration
                }
            // transaction 시작
            try {
                connection.multi()
                logger.info { "기존에 있는 $type 삭제" }
                connection.del(key)
                logger.info { "$type redis에 저장 시작" }
                connection.set(
                    key,
                    token.toByteArray(),
                )
                connection.expire(
                    key,
                    expiration,
                )
                connection.exec()
            } catch (e: Exception) {
                logger.warn { "redis $type 저장 중 오류 발생, message: ${e.message}" }
                throw CommonException(ErrorCode.TOKEN_GENERATION_ERROR)
            }
        }
    }

    private fun deleteAccessToken(email: String) {
        redisTemplate
            .runCatching {
                delete("$REDIS_AT_PREFIX$email")
            }.getOrElse { logger.warn { "redis에 존재하는 at 삭제 실패, message: ${it.message}" } }
    }
}