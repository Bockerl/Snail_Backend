package com.bockerl.snailmember.security

import com.bockerl.snailmember.common.ResponseDTO
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.command.application.service.CommandMemberService
import com.bockerl.snailmember.member.command.domain.aggregate.vo.request.MemberEmailLoginRequestVO
import com.bockerl.snailmember.member.query.service.QueryMemberService
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.io.IOException
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.crypto.spec.SecretKeySpec

class AuthenticationFilter(
    private val queryMemberService: QueryMemberService,
    private val commandMemberService: CommandMemberService,
    private val redisTemplate: RedisTemplate<String, String>,
    @Value("\${TOKEN_SECRET}")
    private val tokenSecret: String,
    @Value("\${ACCESS_TOKEN_EXPIRATION}")
    private val accessTokenExpiration: Long,
    @Value("\${REFRESH_TOKEN_EXPIRATION}")
    private val refreshTokenExpiration: Long,
    @Value("\${TOKEN_ISSUER}")
    private val tokenIssuer: String,
) : UsernamePasswordAuthenticationFilter() {
    private val log = KotlinLogging.logger {}

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        log.info { "이메일과 비밀번호로 로그인 시작" }
        log.info { "request local port: ${request.localPort}" }

        return try {
            val credential = ObjectMapper().readValue(request.inputStream, MemberEmailLoginRequestVO::class.java)
            log.info { "credential 객체 정보: $credential" }

            val member = queryMemberService.loadUserByUsername(credential.memberEmail) as CustomMember

            // 인증 토큰 생성
            val authToken = UsernamePasswordAuthenticationToken(
                member,
                credential,
                member.authorities,
            )
            log.info { "생성된 인증 토큰: $authToken" }
            // 인증 토큰 전달(인증 수행)
            authenticationManager.authenticate(authToken)
        } catch (e: IOException) {
            log.error { "이메일 기반 유저 인증 실패" }
            throw CommonException(ErrorCode.AUTH_TOKEN_GENERATION_ERROR)
        }
    }

    override fun successfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
        authResult: Authentication,
    ) {
        log.info { "Authentication 인증 객체 정보: $authResult" }

        val customMember = authResult.credentials as CustomMember
        val roles = authResult.authorities.map { it.authority }
        val accessExpiration = System.currentTimeMillis() + accessTokenExpiration
        val refreshExpiration = System.currentTimeMillis() + refreshTokenExpiration

        // accessToken에 memberEmail, memberNickname, memberPhoto 넣을 예정
        val accessClaims = Jwts.claims().apply {
            subject = customMember.memberEmail
        }
        accessClaims["auth"] = roles
        accessClaims["memberNickname"] = customMember.memberNickname
        accessClaims["memberId"] = customMember.memberId
        accessClaims["memberPhoto"] = customMember.memberPhoto

        val refreshClaims = Jwts.claims().apply {
            subject = customMember.memberEmail
        }
        refreshClaims["auth"] = roles

        val accessToken: String = Jwts.builder()
            .setClaims(accessClaims)
            .setIssuedAt(Date())
            .setExpiration(Date(accessExpiration))
            .setIssuer(tokenIssuer)
            .signWith(SecretKeySpec(tokenSecret.toByteArray(), SignatureAlgorithm.HS512.jcaName))
            .compact()!!
        log.info { "생성된 accessToken: $accessToken" }
        val refreshToken: String = Jwts.builder()
            .setClaims(refreshClaims)
            .setIssuedAt(Date())
            .setExpiration(Date(refreshExpiration))
            .setIssuer(tokenIssuer)
            .signWith(SecretKeySpec(tokenSecret.toByteArray(), SignatureAlgorithm.HS512.jcaName))
            .compact()!!
        log.info { "생성된 refreshToken: $refreshToken" }

        // key - RT:email, value - refreshToken, TTL - refreshExpiration
        log.info { "refreshToken redis에 저장 시작" }
        redisTemplate.opsForValue().set(
            "RT:${customMember.memberEmail}",
            refreshToken,
            refreshExpiration,
            TimeUnit.MILLISECONDS,
        )
        log.info { "accessToken redis에 저장 시작" }
        redisTemplate.opsForValue().set(
            "AT:${customMember.memberEmail}",
            accessToken,
            accessTokenExpiration,
            TimeUnit.MILLISECONDS,
        )

        log.info { "refreshToken용 쿠기 생성 시작" }
        // RT는 HttpOnly Cookie에 담기
        val refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true) // document.cookie 공격 불가
            .secure(false) // 개발 환경에선 https가 아니므로 false
            .sameSite("Strict") // csrf 방지
            .path("/api")
            .build()

        log.info { "accessToken 헤더에 담기 시작" }
        response.addHeader("Authorization", "Bearer $accessToken")
        log.info { "refreshToken 쿠키에 담기 시작" }
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString())

        log.info { "멤버 마지막 로그인 시각 변경 시작" }
        commandMemberService.putLastAccessTime(customMember.memberEmail)

        log.info { "ResponseDTO 생성 시작" }
        val responseDTO = ResponseDTO.ok("로그인 성공")

        // JSON 문자열로 변환
        val json = ObjectMapper().writeValueAsString(responseDTO)
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        response.writer.write(json)
    }
}
