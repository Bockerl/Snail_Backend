package com.bockerl.snailmember.security

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.query.service.QueryMemberService
import com.bockerl.snailmember.security.config.TokenType
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.lang.Exception
import java.util.Date
import javax.crypto.spec.SecretKeySpec

@Component
class JwtUtils(
    @Value("\${TOKEN_SECRET}")
    private val tokenSecret: String,
    @Value("\${TOKEN_ISSUER}")
    private val tokenIssuer: String,
    @Value("\${ACCESS_TOKEN_EXPIRATION}")
    private val accessExpiration: Long,
    @Value("\${REFRESH_TOKEN_EXPIRATION}")
    private val refreshExpiration: Long,
    private val memberService: QueryMemberService,
    private val redisTemplate: RedisTemplate<String, String>,
) {
    companion object {
        private const val REDIS_RT_PREFIX = "RT:"
        private const val REDIS_AT_PREFIX = "AT:"
    }

    private val secret = SecretKeySpec(tokenSecret.toByteArray(), SignatureAlgorithm.HS512.jcaName)
    private val logger = KotlinLogging.logger {}

    fun parseClaims(token: String): Claims {
        logger.info { "토큰에서 claim 파싱 시작" }
        return Jwts
            .parserBuilder()
            .setSigningKey(secret)
            .build()
            .parseClaimsJws(token)
            .body
    }

    fun extractAccessToken(request: HttpServletRequest): String? =
        request.getHeader("Authorization")?.let { token ->
            if (token.startsWith("Bearer ")) {
                token.substringAfter("Bearer ")
            } else {
                throw CommonException(ErrorCode.TOKEN_TYPE_ERROR)
            }
        }

    fun extractRefreshToken(request: HttpServletRequest): String {
        logger.info { "쿠키에서 rt 추출 시작" }

        val cookies = request.cookies
        if (cookies == null || cookies.isEmpty()) {
            logger.warn { "쿠키가 null이거나 비어있음" }
            throw CommonException(ErrorCode.COOKIE_ERROR)
        }

        val refreshToken =
            cookies.find { it.name == "refreshToken" }?.value
                ?: run {
                    logger.warn { "쿠키에 rt가 존재하지 않음" }
                    throw CommonException(ErrorCode.TOKEN_TYPE_ERROR)
                }
        return refreshToken
    }

    fun generateAccessToken(claims: Claims): String {
        logger.info { "새로운 at 생성 시작" }
        // 넘어오는 claim은 refreshClaim이므로 sub밖에 없다(email)
        val email = claims.subject ?: throw CommonException(ErrorCode.TOKEN_MALFORMED_ERROR)
        // at용 claim을 위해 유저 조회
        logger.info { "새로운 at를 위해 email로 유저 조회" }
        val member = memberService.loadUserByUsername(email) as CustomMember
        logger.info { "새로운 at를 위한 at용 claim 생성 시작" }
        val accessClaims =
            Jwts.claims().apply {
                subject = email
            }
        val authority = member.authorities.firstOrNull()?.authority
        accessClaims["auth"] = authority
        accessClaims["memberNickname"] = member.memberNickname
        accessClaims["memberId"] = member.memberId
        accessClaims["memberPhoto"] = member.memberPhoto
        val accessTokenExpiration = System.currentTimeMillis() + accessExpiration
        val accessToken =
            Jwts
                .builder()
                // accessClaim으로 제작
                .setClaims(accessClaims)
                .setIssuedAt(Date())
                .setExpiration(Date(accessTokenExpiration))
                .setIssuer(tokenIssuer)
                .signWith(SecretKeySpec(tokenSecret.toByteArray(), SignatureAlgorithm.HS512.jcaName))
                .compact()!!
        logger.info { "새로운 at 생성 성공 - at: $accessToken" }
        saveToken(email, accessToken, TokenType.ACCESS_TOKEN)
        return accessToken
    }

    fun generateRefreshToken(claims: Claims): String {
        logger.info { "새로운 rt 생성 시작" }
        val email = claims.subject ?: throw CommonException(ErrorCode.TOKEN_TYPE_ERROR)
        // rt는 claim 그대로 사용 가능
        val refreshTokenExpiration = System.currentTimeMillis() + refreshExpiration
        val refreshToken =
            Jwts
                .builder()
                .setClaims(claims)
                .setIssuedAt(Date())
                .setIssuer(tokenIssuer)
                .setExpiration(Date(refreshTokenExpiration))
                .signWith(SecretKeySpec(tokenSecret.toByteArray(), SignatureAlgorithm.HS512.jcaName))
                .compact()!!
        logger.info { "새로운 rt 생성 성공 - rt: $refreshToken" }
        saveToken(email, refreshToken, TokenType.REFRESH_TOKEN)
        return refreshToken
    }

    private fun saveToken(
        email: String,
        token: String,
        type: TokenType,
    ) {
        redisTemplate.execute { connection ->
            val key =
                when (type) {
                    TokenType.ACCESS_TOKEN -> "$REDIS_AT_PREFIX$email".toByteArray()
                    TokenType.REFRESH_TOKEN -> "$REDIS_RT_PREFIX$email".toByteArray()
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
}