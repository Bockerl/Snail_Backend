package com.bockerl.snailmember.security

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.query.service.QueryMemberService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.spec.SecretKeySpec

@Component
class JwtUtils(
    @Value("\${TOKEN_SECRET}")
    private val tokenSecret: String,
    @Value("\${TOKEN_ISSUER}")
    private val tokenIssuer: String,
    @Value("\${ACCESS_TOKEN_EXPIRATION}")
    private val accessTokenExpiration: Long,
    @Value("\${REFRESH_TOKEN_EXPIRATION}")
    private val refreshTokenExpiration: Long,
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
        return Jwts.parserBuilder()
            .setSigningKey(secret)
            .build()
            .parseClaimsJws(token)
            .body
    }

    fun extractAccessToken(request: HttpServletRequest): String? = request.getHeader("Authorization")?.let { token ->
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

        val refreshToken = cookies.find { it.name == "refreshToken" }?.value
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
        // 기존 at redis에서 삭제
        redisTemplate.runCatching {
            delete("$REDIS_AT_PREFIX$email")
        }.onSuccess {
            logger.info { "redis에 있던 기존 at 삭제 성공" }
        }.onFailure {
            logger.warn { "redis에 있던 기존 at 삭제 과정 중 실패" }
            throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
        // at용 claim을 위해 유저 조회
        logger.info { "새로운 at를 위해 email로 유저 조회" }
        val member = memberService.loadUserByUsername(email) as CustomMember
        logger.info { "새로운 at를 위한 at용 claim 생성 시작" }
        val accessClaims = Jwts.claims().apply {
            subject = email
        }
        val authority = member.authorities.firstOrNull()?.authority
        accessClaims["auth"] = authority
        accessClaims["memberNickname"] = member.memberNickname
        accessClaims["memberId"] = member.memberId
        accessClaims["memberPhoto"] = member.memberPhoto
        val accessExpiration = System.currentTimeMillis() + accessTokenExpiration
        val accessToken = Jwts.builder()
            // accessClaim으로 제작
            .setClaims(accessClaims)
            .setIssuedAt(Date())
            .setExpiration(Date(accessExpiration))
            .setIssuer(tokenIssuer)
            .signWith(SecretKeySpec(tokenSecret.toByteArray(), SignatureAlgorithm.HS512.jcaName))
            .compact()!!
        logger.info { "새로운 at 생성 성공 - at: $accessToken" }
        redisTemplate.opsForValue().runCatching {
            logger.info { "redis에 새로운 at 저장 시작" }
            set("$REDIS_AT_PREFIX$email", accessToken)
        }.onSuccess {
            logger.info { "redis에서 새로운 at 저장 성공" }
        }.onFailure {
            logger.warn { "redis에 새로운 at 저장 과정 중 실패" }
            throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
        return accessToken
    }

    fun generateRefreshToken(claims: Claims): String {
        logger.info { "새로운 rt 생성 시작" }
        val email = claims.subject ?: throw CommonException(ErrorCode.TOKEN_TYPE_ERROR)
        // 기존 rt redis에서 삭제
        redisTemplate.runCatching {
            delete("$REDIS_RT_PREFIX$email")
        }.onSuccess {
            logger.info { "redis에 있던 기존 rt 삭제 성공" }
        }.onFailure {
            logger.warn { "redis에 있던 기존 rt 삭제 과정 중 실패" }
            throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
        // rt는 claim 그대로 사용 가능
        val refreshExpiration = System.currentTimeMillis() + refreshTokenExpiration
        val refreshToken = Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(Date())
            .setIssuer(tokenIssuer)
            .setExpiration(Date(refreshExpiration))
            .signWith(SecretKeySpec(tokenSecret.toByteArray(), SignatureAlgorithm.HS512.jcaName))
            .compact()!!
        logger.info { "새로운 rt 생성 성공 - rt: $refreshToken" }
        redisTemplate.opsForValue().runCatching {
            logger.info { "redis에 새로운 rt 저장 시작" }
            set("$REDIS_RT_PREFIX$email", refreshToken)
        }.onSuccess {
            logger.info { "redis에서 새로운 rt 저장 성공" }
        }.onFailure {
            logger.warn { "redis에 새로운 rt 저장 과정 중 실패" }
            throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
        }
        return refreshToken
    }
}
