package com.bockerl.snailmember.security

import com.bockerl.snailmember.common.ResponseDTO
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.command.application.service.CommandMemberService
import com.bockerl.snailmember.member.command.domain.vo.request.MemberEmailLoginRequestVO
import com.bockerl.snailmember.member.query.service.QueryMemberService
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.env.Environment
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.lang.Exception
import java.util.Date
import javax.crypto.spec.SecretKeySpec

class AuthenticationFilter(
    private val queryMemberService: QueryMemberService,
    private val commandMemberService: CommandMemberService,
    private val redisTemplate: RedisTemplate<String, String>,
    private val authenticationManager: AuthenticationManager,
    private val environment: Environment,
) : UsernamePasswordAuthenticationFilter() {
    private val log = KotlinLogging.logger {}

    override fun attemptAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Authentication {
        log.info { "이메일과 비밀번호로 로그인 시작" }
        log.info { "request local port: ${request.localPort}" }

        return try {
            val credential = ObjectMapper().readValue(request.inputStream, MemberEmailLoginRequestVO::class.java)
            log.info { "credential 객체 정보: $credential" }

            val member = queryMemberService.loadUserByUsername(credential.memberEmail) as CustomMember
            log.info { "mail로 조회된 회원 정보(CustomUser): $member" }

            // 인증 토큰 생성
            val authToken =
                UsernamePasswordAuthenticationToken(
                    member,
                    null,
                    member.authorities,
                )
            log.info { "생성된 인증 토큰: $authToken" }
            // 인증 토큰 전달(인증 수행)
            authenticationManager.authenticate(authToken)
        } catch (e: Exception) {
            log.error { "이메일 기반 유저 인증 실패, ${e.message}, ${e.javaClass}" }
            throw e
        }
    }

    override fun successfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
        authResult: Authentication,
    ) {
        log.info { "Authentication 인증 객체 정보: $authResult" }
        // 환경 변수 관리
        val accessExpiration =
            environment.getProperty("ACCESS_TOKEN_EXPIRATION")?.toLong()
                ?: throw CommonException(ErrorCode.NOT_FOUND_ENV)
        val refreshExpiration =
            environment.getProperty("REFRESH_TOKEN_EXPIRATION")?.toLong()
                ?: throw CommonException(ErrorCode.NOT_FOUND_ENV)
        val tokenIssuer =
            environment.getProperty("TOKEN_ISSUER")
                ?: throw CommonException(ErrorCode.NOT_FOUND_ENV)
        val tokenSecret =
            environment.getProperty("TOKEN_SECRET")
                ?: throw CommonException(ErrorCode.NOT_FOUND_ENV)

        val customMember = authResult.principal as CustomMember
        val authority = customMember.authorities.firstOrNull()?.authority
        val accessTokenExpiration = System.currentTimeMillis() + accessExpiration * 1000
        val refreshTokenExpiration = System.currentTimeMillis() + refreshExpiration * 1000
        // accessToken에 memberEmail, memberNickname, memberPhoto 넣을 예정
        val accessClaims =
            Jwts.claims().apply {
                subject = customMember.memberEmail
            }
        accessClaims["auth"] = authority
        accessClaims["memberNickname"] = customMember.memberNickname
        accessClaims["memberId"] = customMember.memberId
        accessClaims["memberPhoto"] = customMember.memberPhoto
        val refreshClaims =
            Jwts.claims().apply {
                subject = customMember.memberEmail
            }
        refreshClaims["auth"] = authority
        val accessToken: String =
            Jwts
                .builder()
                .setClaims(accessClaims)
                .setIssuedAt(Date())
                .setExpiration(Date(accessTokenExpiration))
                .setIssuer(tokenIssuer)
                .signWith(SecretKeySpec(tokenSecret.toByteArray(), SignatureAlgorithm.HS512.jcaName))
                .compact()!!
        log.info { "생성된 accessToken: $accessToken" }
        val refreshToken: String =
            Jwts
                .builder()
                .setClaims(refreshClaims)
                .setIssuedAt(Date())
                .setExpiration(Date(refreshTokenExpiration))
                .setIssuer(tokenIssuer)
                .signWith(SecretKeySpec(tokenSecret.toByteArray(), SignatureAlgorithm.HS512.jcaName))
                .compact()!!
        log.info { "생성된 refreshToken: $refreshToken" }
        // key - RT:email, value - refreshToken, TTL - refreshExpiration
        redisTemplate.execute { connection ->
            // transaction 시작
            try {
                connection.multi()
                log.info { "refreshToken redis에 저장 시작" }
                val refreshKey = "RT:${customMember.memberEmail}".toByteArray()
                connection.set(
                    refreshKey,
                    refreshToken.toByteArray(),
                )
                connection.expire(
                    refreshKey,
                    refreshExpiration,
                )
                log.info { "accessToken redis에 저장 시작" }
                val accessKey = "AT:${customMember.memberEmail}".toByteArray()
                connection.set(
                    accessKey,
                    accessToken.toByteArray(),
                )
                connection.expire(
                    accessKey,
                    accessExpiration,
                )
                val result = connection.exec()
                result.size == 4 && result.all { it is Boolean }
            } catch (e: Exception) {
                log.warn { "redis 토큰 저장 중 오류 발생, message: ${e.message}" }
                throw CommonException(ErrorCode.TOKEN_GENERATION_ERROR)
            }
        }
        log.info { "rt와 at를 header에 담기 시작" }
        // at는 Header, rt는 Body
        response.setHeader("Authorization", "Bearer $accessToken")
        response.setHeader("refreshToken", refreshToken)

        val ipAddress = request.remoteAddr
        val userAgent = request.getHeader("User-Agent")
        val idempotencyKey = request.getHeader("IdempotencyKey")
        commandMemberService.putLastAccessTime(customMember.memberEmail, ipAddress, userAgent, idempotencyKey)

        log.info { "ResponseDTO 생성 시작" }
        val responseDTO = ResponseDTO.ok("로그인 성공")
        // JSON 문자열로 변환
        val json = ObjectMapper().writeValueAsString(responseDTO)
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        response.writer.write(json)
    }
}