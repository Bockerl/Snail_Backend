package com.bockerl.snailmember.security

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.command.application.dto.response.LoginResponseDTO
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.core.env.Environment
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.spec.SecretKeySpec

@Component
class Oauth2JwtUtils(environment: Environment, private val redisTemplate: RedisTemplate<String, String>) {

    companion object {
        private const val REDIS_RT_PREFIX = "RT:"
        private const val REDIS_AT_PREFIX = "AT:"
    }

    private val logger = KotlinLogging.logger {}

    // 환경 변수 관리
    private val accessTokenExpiration =
        environment.getProperty("ACCESS_TOKEN_EXPIRATION")?.toLong()
            ?: throw CommonException(ErrorCode.NOT_FOUND_ENV)
    private val refreshTokenExpiration =
        environment.getProperty("REFRESH_TOKEN_EXPIRATION")?.toLong()
            ?: throw CommonException(ErrorCode.NOT_FOUND_ENV)
    private val tokenIssuer =
        environment.getProperty("TOKEN_ISSUER")
            ?: throw CommonException(ErrorCode.NOT_FOUND_ENV)
    private val tokenSecret =
        environment.getProperty("TOKEN_SECRET")
            ?: throw CommonException(ErrorCode.NOT_FOUND_ENV)

    fun generateJwtResponse(customMember: CustomMember): LoginResponseDTO {
        logger.info { "Oauth2 회원의 토큰 생성 시작" }
        val email = customMember.memberEmail
        // AccessToken 생성 및 저장
        val accessToken = generateAccessToken(customMember)
        // RefreshToken 처리
        val refreshToken = getOrCreateRefreshToken(email, customMember.authorities.firstOrNull()?.authority)
        return LoginResponseDTO(accessToken, refreshToken)
    }

    private fun generateAccessToken(customMember: CustomMember): String {
        logger.info { "AccessToken 생성 시작" }
        val email = customMember.memberEmail
        // 기존 AccessToken 삭제
        deleteAccessToken(email)
        // AccessToken Claims 생성
        val claims = Jwts.claims().apply {
            subject = customMember.memberEmail
        }
        val authority = customMember.authorities.firstOrNull()?.authority
        claims["auth"] = authority
        claims["memberNickname"] = customMember.memberNickname
        claims["memberId"] = customMember.memberId
        claims["memberPhoto"] = customMember.memberPhoto
        // AccessToken 생성
        val accessExpiration = System.currentTimeMillis() + accessTokenExpiration
        val accessToken = generateToken(claims, accessExpiration)
        // Redis에 저장
        saveAccessToken(accessToken, email)
        logger.info { "AccessToken 생성 완료" }
        return accessToken
    }

    private fun getOrCreateRefreshToken(email: String, authority: String?): String {
        logger.info { "RefreshToken 처리 시작" }
        // Redis에서 기존 RefreshToken 조회
        val existingRefreshToken = redisTemplate.opsForValue().get("$REDIS_RT_PREFIX$email")
        // 기존 토큰이 있으면 재사용, 없으면 새로 생성
        return if (existingRefreshToken != null) {
            logger.info { "기존 RefreshToken 재사용" }
            existingRefreshToken
        } else {
            logger.info { "새 RefreshToken 생성" }
            val refreshClaims = Jwts.claims().apply {
                subject = email
                this["auth"] = authority
            }
            val refreshExpiration = System.currentTimeMillis() + refreshTokenExpiration
            generateToken(refreshClaims, refreshExpiration)
        }
    }

    private fun generateToken(claims: Claims?, expiration: Long): String {
        val token: String = Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(Date())
            .setExpiration(Date(expiration))
            .setIssuer(tokenIssuer)
            .signWith(SecretKeySpec(tokenSecret.toByteArray(), SignatureAlgorithm.HS512.jcaName))
            .compact()!!
        return token
    }

    private fun saveAccessToken(accessToken: String, email: String) {
        redisTemplate.opsForValue().runCatching {
            logger.info { "새로 생성된 at - redis에 저장: $accessToken" }
            set("$REDIS_AT_PREFIX$email", accessToken)
        }.onSuccess {
            logger.info { "redis에 새로운 at 저장 성공" }
        }.onFailure {
            logger.warn { "redis에 새로운 at 저장 실패" }
        }
    }

    private fun deleteAccessToken(email: String) {
        redisTemplate.runCatching {
            delete("$REDIS_AT_PREFIX$email")
        }.onSuccess {
            logger.info { "redis에 존재하는 at 삭제 성공" }
        }.onFailure {
            logger.warn { "redis에 존재하는 at 삭제 실패" }
        }
    }
}
