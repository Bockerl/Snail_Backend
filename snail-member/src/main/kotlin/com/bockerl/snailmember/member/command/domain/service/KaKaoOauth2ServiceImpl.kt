package com.bockerl.snailmember.member.command.domain.service

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.client.KaKaoAuthClient
import com.bockerl.snailmember.member.command.application.dto.response.KaKaoPayloadDTO
import com.bockerl.snailmember.member.command.application.dto.response.LoginResponseDTO
import com.bockerl.snailmember.member.command.application.service.KaKaoOauth2Service
import com.bockerl.snailmember.member.command.config.Oauth2LoginProperties
import com.bockerl.snailmember.member.command.domain.aggregate.entity.Gender
import com.bockerl.snailmember.member.command.domain.aggregate.entity.Language
import com.bockerl.snailmember.member.command.domain.aggregate.entity.Member
import com.bockerl.snailmember.member.command.domain.aggregate.entity.MemberStatus
import com.bockerl.snailmember.member.command.domain.aggregate.entity.SignUpPath
import com.bockerl.snailmember.member.command.domain.repository.MemberRepository
import com.bockerl.snailmember.security.CustomMember
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.core.env.Environment
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.Date
import java.util.UUID
import javax.crypto.spec.SecretKeySpec

@Service
class KaKaoOauth2ServiceImpl(
    private val memberRepository: MemberRepository,
    private val loginProperties: Oauth2LoginProperties,
    private val kakaoAuthClient: KaKaoAuthClient,
    environment: Environment,
    private val redisTemplate: RedisTemplate<String, String>,
    // 추후에 jwtToken 발행도 덧붙일 것
) : KaKaoOauth2Service {
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

    @Transactional
    override fun kakaoLogin(code: String): LoginResponseDTO {
        // 코드 기반으로 인증 토큰 요청
        val idToken = requestTokenFromKaKao(code)
        // 유저 정보를 조회
        val customMember = decodeUserInfoFromToken(idToken) as CustomMember
        // rt 및 at 생성해서 넣어두기
        val responseDTO = generateJwtResponse(customMember)
        return responseDTO
    }

    private fun requestTokenFromKaKao(code: String): String {
        logger.info { "카카오 코드 기반 인증 토큰 요청 시작" }
        logger.info { "clientId: ${loginProperties.kakaoClientId}" }
        logger.info { "redirectUri: ${loginProperties.kakaoRedirectUri}" }
        logger.info { "code: $code" }
        logger.info { "clientSecret: ${loginProperties.kakaoClientSecret}" }
        return try {
            val response = kakaoAuthClient.getAccessToken(
                clientId = loginProperties.kakaoClientId,
                redirectUri = loginProperties.kakaoRedirectUri,
                code = code,
                clientSecret = loginProperties.kakaoClientSecret,
            )
            logger.info { "카카오로부터 돌아온 토큰 responseDTO: $response" }
            response.idToken
        } catch (e: Exception) {
            logger.error(e) { "카카오 Access Token 요청 실패" }
            throw e
        }
    }

    private fun decodeUserInfoFromToken(idToken: String): UserDetails {
        logger.info { "카카오 ID 토큰으로 유저 정보 디코딩 시작" }
        return try {
            // JWT 토큰의 payload 부분 추출 및 디코딩
            val payload = idToken.split(".")[1]
            val decodedBytes = Base64.getUrlDecoder()
                .decode(payload.padEnd((payload.length + 3) / 4 * 4, '='))
            val decodedString = String(decodedBytes)
            logger.info { "디코딩된 페이로드 전체: $decodedString" }

            // JSON 파싱
            val jsonObject = ObjectMapper().readTree(decodedString)

            // sub 필드가 카카오 회원 번호
            val kakaoId = jsonObject["sub"].asText()
            logger.info { "카카오 제공 고유 id: $kakaoId" }

            // 이메일 생성
            val email = "$kakaoId@kakao.com"

            // 카카오 응답 데이터 구성
            val kakaoResponse = KaKaoPayloadDTO(
                id = kakaoId.toLong(),
                email = email,
                nickname = jsonObject["nickname"]?.asText(),
                birthday = jsonObject["birthday"]?.asText(),
                birthyear = jsonObject["birthyear"]?.asText(),
            )

            logger.info { "디코딩된 카카오 계정 유저 정보: $kakaoResponse" }

            // 기존 회원 조회 또는 새 회원 생성
            val member = memberRepository.findMemberByMemberEmail(email)
                ?: createNewKaKaoMember(email, kakaoResponse)
            if (member.memberStatus == MemberStatus.ROLE_BLACKLIST) {
                logger.warn { "카카오 블랙 리스트 멤버가 로그인 - email: $email" }
                throw CommonException(ErrorCode.BLACK_LIST_ROLE)
            }

            val authority = listOf(SimpleGrantedAuthority(member.memberStatus.toString()))

            CustomMember(member, authority)
        } catch (e: Exception) {
            logger.error(e) { "카카오 ID 토큰 디코딩 실패" }
            throw e
        }
    }

    private fun createNewKaKaoMember(email: String, kakaoResponse: KaKaoPayloadDTO): Member {
        val birth = if (!kakaoResponse.birthyear.isNullOrEmpty() && !kakaoResponse.birthday.isNullOrEmpty()) {
            "${kakaoResponse.birthyear}${kakaoResponse.birthday}".toLocalDateOrNow()
        } else {
            LocalDate.now()
        }

        val newKaKaoMember = Member(
            memberEmail = email,
            memberPhoneNumber = "FromKaKao",
            memberPhoto = "",
            memberStatus = MemberStatus.ROLE_USER,
            memberRegion = "",
            memberLanguage = Language.KOR,
            memberGender = Gender.UNKNOWN,
            memberNickName = kakaoResponse.nickname ?: UUID.randomUUID().toString(),
            memberBirth = birth,
            memberPassword = UUID.randomUUID().toString(),
            signupPath = SignUpPath.Kakao,
            selfIntroduction = "",
        )

        logger.info { "새로 생성되는 카카오 계정 멤버: $newKaKaoMember" }
        memberRepository.save(newKaKaoMember)
        logger.info { "카카오 계정 새 멤버 저장 성공" }
        return newKaKaoMember
    }

    private fun generateJwtResponse(customMember: CustomMember): LoginResponseDTO {
        logger.info { "카카오 회원의 토큰 생성 시작" }
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

    private fun String.toLocalDateOrNow(): LocalDate = try {
        LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyyMMdd"))
    } catch (e: Exception) {
        logger.warn { "생년월일 파싱 실패 (입력값: $this): ${e.message}" }
        LocalDate.now()
    }
}
