package com.bockerl.snailmember.security

import com.bockerl.snailmember.common.ResponseDTO
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.command.application.service.CommandMemberService
import com.bockerl.snailmember.member.command.domain.aggregate.vo.response.LoginResponseVO
import com.bockerl.snailmember.member.query.service.QueryMemberService
import com.bockerl.snailmember.security.config.TokenType
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.env.Environment
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import java.lang.Exception

class JwtFilter(
    private val queryMemberService: QueryMemberService,
    private val commandMemberService: CommandMemberService,
    private val jwtUtils: JwtUtils,
    private val redisTemplate: RedisTemplate<String, String>,
    environment: Environment,
) : OncePerRequestFilter() {
    private val log = KotlinLogging.logger {}

    companion object {
        private const val REDIS_RT_PREFIX = "RT:"
        private const val REDIS_AT_PREFIX = "AT:"
    }

    // 환경 변수 관리
    private val refreshTokenExpiration =
        environment.getProperty("REFRESH_TOKEN_EXPIRATION")?.toLong()
            ?: throw CommonException(ErrorCode.NOT_FOUND_ENV)
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
            path.startsWith("/api/member/health")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            log.info { "Jwt 인증 시작" }
            val accessToken = jwtUtils.extractAccessToken(request)
            if (accessToken != null && processAccessToken(accessToken)) {
                log.info { "accessToken 유효성 검사 성공" }
                filterChain.doFilter(request, response)
                return
            }
            log.info { "at가 없거나 유효하지 않음, rt 검증 시작" }
            if (processRefreshToken(request, response)) {
                log.info { "rt 유효성 검사 성공" }
                filterChain.doFilter(request, response)
                return
            }
            log.warn { "인증 실패, at와 rt 모두 유효하지 않음" }
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰 인증이 필요합니다.")
        } catch (e: CommonException) {
            log.error { "토큰 인증 중 에러 발생: ${e.message}" }
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                e.message,
            )
        }
    }

    private fun processAccessToken(accessToken: String): Boolean = try {
        val claims = jwtUtils.parseClaims(accessToken)
        validateClaims(claims, accessToken, TokenType.ACCESS_TOKEN)
    } catch (e: ExpiredJwtException) {
        log.warn { "만료된 accessToken: ${e.message}" }
        false
    } catch (e: Exception) {
        log.warn { "accessToken 유효성 검사 중 에러 발생: ${e.message}" }
        false
    }

    private fun processRefreshToken(request: HttpServletRequest, response: HttpServletResponse): Boolean {
        return try {
            val refreshToken = jwtUtils.extractRefreshToken(request)
            val claims = jwtUtils.parseClaims(refreshToken)
            if (!validateClaims(claims, refreshToken, TokenType.REFRESH_TOKEN)) {
                sendErrorResponse(response, ErrorCode.INVALID_TOKEN_ERROR)
                return false
            }
            processValidRefreshToken(claims, refreshToken, response)
            true
        } catch (e: ExpiredJwtException) {
            log.warn { "만료된 refreshToken: ${e.message}" }
            sendErrorResponse(response, ErrorCode.EXPIRED_TOKEN_ERROR)
            false
        } catch (e: Exception) {
            log.warn { "refreshToken 유효성 검사 중 에러 발생: ${e.message}" }
            sendErrorResponse(response, ErrorCode.INTERNAL_SERVER_ERROR)
            false
        }
    }

    private fun processValidRefreshToken(claims: Claims, refreshToken: String, response: HttpServletResponse) {
        // 액세스 토큰 발급
        val newAccessToken = jwtUtils.generateAccessToken(claims)
        // Refresh Token 만료 기간 확인
        val remainingTime = claims.expiration.time - System.currentTimeMillis()
        val sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L
        // RT가 7일 이하로 남았으면 새로 발급, 아니면 기존 것 사용
        val finalRefreshToken = if (remainingTime <= sevenDaysInMillis) {
            log.info { "RT 만료 기간이 7일 이하로 남아 새로 발급" }
            jwtUtils.generateRefreshToken(claims)
        } else {
            refreshToken
        }
        // 사용자 마지막 로그인 시간 업데이트
        updateLastAccessTime(claims.subject)
        // 응답 데이터 생성 및 전송
        sendTokenResponse(response, newAccessToken, finalRefreshToken)
    }

    private fun updateLastAccessTime(email: String) {
        commandMemberService.runCatching {
            log.info { "멤버 마지막 로그인 시각 변경 시작" }
            putLastAccessTime(email)
        }.onSuccess {
            log.info { "멤버 마지막 로그인 시각 변경 성공" }
        }.onFailure { e ->
            log.warn { "멤버 마지막 로그인 시각 변경 실패: ${e.message}" }
        }
    }

    private fun sendTokenResponse(response: HttpServletResponse, accessToken: String, refreshToken: String) {
        val loginVO = LoginResponseVO(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
        val responseDTO = ResponseDTO.ok(loginVO)
        sendJsonResponse(response, responseDTO)
    }

    private fun sendErrorResponse(response: HttpServletResponse, errorCode: ErrorCode) {
        val responseDTO = ResponseDTO.fail(CommonException(errorCode))
        sendJsonResponse(response, responseDTO)
    }

    private fun sendJsonResponse(response: HttpServletResponse, responseDTO: ResponseDTO<*>) {
        val json = ObjectMapper().writeValueAsString(responseDTO)
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        response.writer.write(json)
    }

    private fun validateClaims(claims: Claims, token: String, type: TokenType): Boolean {
        log.info { "claims 유효성 검사 시작" }
        // isser 검증
        val issuer = claims.issuer
        if (issuer != tokenIssuer) {
            log.warn { "유효하지 않은 발행처: $issuer" }
            return false
        }
        log.info { "isser 통과" }
        // subject 추출
        val email = claims.subject ?: throw CommonException(ErrorCode.TOKEN_MALFORMED_ERROR)
        log.info { "subject 통과, sub: $email" }
        // auth 추출
        val auth = claims["auth"] as? List<*> ?: throw CommonException(ErrorCode.TOKEN_MALFORMED_ERROR)
        if (auth.isEmpty()) {
            log.warn { "권한 정보가 없는 $type" }
            return false
        }

        val prefix = when (type) {
            TokenType.ACCESS_TOKEN -> REDIS_AT_PREFIX
            TokenType.REFRESH_TOKEN -> REDIS_RT_PREFIX
        }
        // redis에 저장된 at와 비교
        val redisToken = redisTemplate.opsForValue().get("$prefix$email")
            ?: throw CommonException(ErrorCode.EXPIRED_TOKEN_ERROR)
        if (redisToken != token) {
            log.warn { "redis에 저장된 $type 과 일치하지 않음" }
            throw CommonException(ErrorCode.INVALID_TOKEN_ERROR)
        }
        log.info { "redis 토큰과 일치" }
        // auth 검증
        val member = queryMemberService.loadUserByUsername(email)
        val memberAuth = member.authorities.firstOrNull()?.authority
        val tokenAuth = auth[0].toString()
        if (tokenAuth != memberAuth) {
            log.warn { "토큰의 권한($tokenAuth)과 멤버 본래 권한($memberAuth)이 일치하지 않음" }
            return false
        }
        log.info { "auth 통과" }

        // SecurityContext에 설정
        val authentication = UsernamePasswordAuthenticationToken(
            email,
            member.password,
            member.authorities,
        )
        log.info { "securityContextHolder에게 맡길 authentication: $authentication" }
        SecurityContextHolder.getContext().authentication = authentication
        log.info { "securityContextHolder에 저장 성공" }
        return true
    }
}
