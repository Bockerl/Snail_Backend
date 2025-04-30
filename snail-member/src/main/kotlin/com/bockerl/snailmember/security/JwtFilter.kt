package com.bockerl.snailmember.security

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.command.application.service.CommandMemberService
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.MemberStatus
import com.bockerl.snailmember.member.query.service.QueryMemberService
import com.bockerl.snailmember.security.config.TokenType
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.env.Environment
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.authentication.LockedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import java.lang.Exception

class JwtFilter(
    private val queryMemberService: QueryMemberService,
    private val commandMemberService: CommandMemberService,
    private val jwtUtils: JwtUtils,
    private val authenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val eventPublisher: ApplicationEventPublisher,
    private val redisTemplate: RedisTemplate<String, String>,
    environment: Environment,
) : OncePerRequestFilter() {
    private val log = KotlinLogging.logger {}

    companion object {
        private const val REDIS_RT_PREFIX = "RT:"
        private const val REDIS_AT_PREFIX = "AT:"
    }

    // 환경 변수 관리
    private val tokenIssuer =
        environment.getProperty("TOKEN_ISSUER")
            ?: throw CommonException(ErrorCode.NOT_FOUND_ENV)

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        log.info { "요청이 들어온 uri: $path" }
        return path.startsWith("/swagger-ui") or
            path.startsWith("/api/member/login") or
            path.startsWith("/swagger-resources") or
            path.startsWith("/api/registration") or
            path.startsWith("/api/user/oauth2") or
            path.startsWith("/favicon.ico") or
            path.startsWith("/v3/api-docs") or
            path.startsWith("/api/member/health") or
            path.startsWith("/api/area/")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            log.info { "Jwt 인증 시작" }
            val accessToken = jwtUtils.extractAccessToken(request)
            if (!accessToken.isNullOrBlank()) {
                processAccessToken(accessToken)
            } else {
                log.info { "AccessToken 비어있음, RefreshToken 검증 시작" }
                processRefreshToken(request, response)
            }
            log.info { "토큰 인증 통과" }
            filterChain.doFilter(request, response)
        } catch (e: AuthenticationException) {
            log.error { "AuthenticationException 발생: ${e.javaClass.simpleName} - ${e.message}" }
            SecurityContextHolder.clearContext()
            authenticationEntryPoint.commence(request, response, e)
        }
    }

    private fun processAccessToken(accessToken: String): Boolean =
        try {
            log.info { "accessToken 유효성 검사 시작" }
            val claims = jwtUtils.parseClaims(accessToken)
            validateClaims(claims, accessToken, TokenType.ACCESS_TOKEN)
        } catch (e: ExpiredJwtException) {
            log.warn { "만료된 accessToken: ${e.message}" }
            throw InsufficientAuthenticationException(e.message, e)
        } catch (e: MalformedJwtException) {
            log.warn { "Malformed accessToken 검출: ${e.message}" }
            throw BadCredentialsException(e.message, e)
        } catch (e: AuthenticationException) {
            log.warn { "accessToken Authentication 예외 발생: ${e.message}" }
            throw e
        } catch (e: Exception) {
            log.warn { "accessToken 유효성 검사 중 에러 발생, message: ${e.message}, ex: ${e.javaClass.name}" }
            throw AuthenticationServiceException("AccessToken 검증 중 알 수 없는 오류 발생", e)
        }

    private fun processRefreshToken(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Boolean {
        return try {
            val refreshToken = jwtUtils.extractRefreshToken(request)
            val claims = jwtUtils.parseClaims(refreshToken)
            if (!validateClaims(claims, refreshToken, TokenType.REFRESH_TOKEN)) {
                return false
            }
            processValidRefreshToken(claims, refreshToken, request, response)
            true
        } catch (e: ExpiredJwtException) {
            log.warn { "만료된 refreshToken: ${e.message}" }
            throw InsufficientAuthenticationException(e.message, e)
        } catch (e: MalformedJwtException) {
            log.warn { "Malformed refreshToken 검출: ${e.message}" }
            throw BadCredentialsException(e.message, e)
        } catch (e: AuthenticationException) {
            log.warn { "refreshToken Authentication 예외 발생: ${e.message}" }
            throw e
        } catch (e: Exception) {
            log.warn { "refreshToken 유효성 검사 중 에러 발생, message: ${e.message}, ex: ${e.javaClass.name}" }
            throw AuthenticationServiceException("AccessToken 검증 중 알 수 없는 오류 발생", e)
        }
    }

    private fun processValidRefreshToken(
        claims: Claims,
        refreshToken: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        // 액세스 토큰 발급
        val newAccessToken = jwtUtils.generateAccessToken(claims)
        // Refresh Token 만료 기간 확인
        val remainingTime = claims.expiration.time - System.currentTimeMillis()
        val sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L
        log.info { "RT 만료기간 검증 시작(7일 이내)" }
        // RT가 7일 이하로 남았으면 새로 발급, 아니면 기존 것 사용
        val finalRefreshToken =
            if (remainingTime <= sevenDaysInMillis) {
                log.info { "RT 만료 기간이 7일 이하로 남아 새로 발급" }
                jwtUtils.generateRefreshToken(claims)
            } else {
                refreshToken
            }
        // 사용자 마지막 로그인 시간 업데이트
        val ipAddress = request.remoteAddr
        log.info { "사용자 ipAddress: $ipAddress" }
        val userAgent = request.getHeader("User-Agent")
        log.info { "사용자 userAgent: $userAgent" }
        val idempotencyKey =
            request.getHeader("IdempotencyKey")
                ?: throw InsufficientAuthenticationException("Idempotency-Key가 누락되었습니다.")
        log.info { "사용자 IdempotencyKey: $idempotencyKey" }
        commandMemberService.putLastAccessTime(claims.subject, ipAddress, userAgent, idempotencyKey)
        // 새로운 at Header에 담기
        response.setHeader("Authorization", "Bearer $newAccessToken")
        // RT Header에 담기
        response.setHeader("refreshToken", finalRefreshToken)
    }

    private fun validateClaims(
        claims: Claims,
        token: String,
        type: TokenType,
    ): Boolean {
        log.info { "claims 유효성 검사 시작" }
        // isser 검증
        val issuer = claims.issuer
        if (issuer != tokenIssuer) {
            log.warn { "유효하지 않은 발행처: $issuer" }
            throw BadCredentialsException("유효하지 않은 issuer: $issuer")
        }
        log.info { "issuer 통과" }
        // subject 추출
        val email = claims.subject ?: throw BadCredentialsException("$type's email이 null입니다.")
        log.info { "subject 통과, sub: $email" }
        // auth 추출
        val auth = claims["auth"] as? String ?: throw BadCredentialsException("$type's auth가 null입니다.")
        log.info { "auth: $auth" }
        if (auth.isBlank()) {
            log.warn { "권한 정보가 없는 $type" }
            throw BadCredentialsException("$type's auth이 빈칸입니다.")
        } else if (auth == MemberStatus.ROLE_BLACKLIST.toString()) {
            log.warn { "블랙리스트 접근, email: $email" }
            throw LockedException("블랙리스트 계정입니다.")
        }
        val prefix =
            when (type) {
                TokenType.ACCESS_TOKEN -> REDIS_AT_PREFIX
                TokenType.REFRESH_TOKEN -> REDIS_RT_PREFIX
            }
        // redis에 저장된 at와 비교
        log.info { "redis로 조회할 key: $prefix$email" }
        val redisToken =
            redisTemplate.opsForValue().get("$prefix$email")
                ?: {
                    logger.info { "$prefix$email 로 조회된 토큰이 없음" }
                    throw CredentialsExpiredException("Redis에 저장된 토큰이 없음 또는 만료됨")
                }
        if (redisToken != token) {
            log.warn { "redis에 저장된 $type 과 일치하지 않음" }
            throw InsufficientAuthenticationException("부적절한 $type 입니다.")
        }
        log.info { "redis 토큰과 일치" }
        // auth 검증
        val member = queryMemberService.loadUserByUsername(email)
        val memberAuth = member.authorities.firstOrNull()?.authority
        if (auth != memberAuth) {
            log.warn { "토큰의 권한($auth)과 멤버 본래 권한($memberAuth)이 일치하지 않음" }
            throw BadCredentialsException("회원과 일치하지 않은 권한입니다.")
        }
        log.info { "auth 통과" }
        // SecurityContext에 설정
        val authentication =
            UsernamePasswordAuthenticationToken(
                member,
                null,
                member.authorities,
            )
        log.info { "securityContextHolder에게 맡길 authentication: $authentication" }
        SecurityContextHolder.getContext().authentication = authentication
        log.info { "securityContextHolder에 저장 성공" }
        return true
    }
}