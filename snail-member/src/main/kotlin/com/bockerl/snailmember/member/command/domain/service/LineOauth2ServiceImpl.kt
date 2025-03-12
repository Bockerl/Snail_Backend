package com.bockerl.snailmember.member.command.domain.service

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.infrastructure.config.TransactionalConfig
import com.bockerl.snailmember.member.client.LineAuthClient
import com.bockerl.snailmember.member.command.application.dto.response.LinePayloadDTO
import com.bockerl.snailmember.member.command.application.dto.response.LoginResponseDTO
import com.bockerl.snailmember.member.command.application.service.LineOauth2Service
import com.bockerl.snailmember.member.command.config.Oauth2LoginProperties
import com.bockerl.snailmember.member.command.domain.aggregate.entity.Gender
import com.bockerl.snailmember.member.command.domain.aggregate.entity.Language
import com.bockerl.snailmember.member.command.domain.aggregate.entity.Member
import com.bockerl.snailmember.member.command.domain.aggregate.entity.MemberStatus
import com.bockerl.snailmember.member.command.domain.aggregate.entity.SignUpPath
import com.bockerl.snailmember.member.command.domain.repository.MemberRepository
import com.bockerl.snailmember.security.CustomMember
import com.bockerl.snailmember.security.Oauth2JwtUtils
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.Base64
import java.util.UUID

@Service
class LineOauth2ServiceImpl(
    private val lineAuthClient: LineAuthClient,
    private val memberRepository: MemberRepository,
    private val loginProperties: Oauth2LoginProperties,
    private val jwtUtils: Oauth2JwtUtils,
    // jwt 추가 예정
) : LineOauth2Service {
    private val logger = KotlinLogging.logger {}

    @Transactional
    override fun lineLogin(code: String): LoginResponseDTO {
        // 코드 기반으로 요청을 보낸 뒤 id token만 추출
        val idToken = requestTokenFromLine(code)
        // 유저 정보 디코딩
        val customMember = TransactionalConfig.run { decodeUserInfoFromToken(idToken) as CustomMember }
        val response = jwtUtils.generateJwtResponse(customMember)
        return response
    }

    private fun requestTokenFromLine(code: String): String {
        logger.info { "라인 코드 기반 인증 토큰 요청 시작" }
        logger.info { "clientId: ${loginProperties.lineClientId}" }
        logger.info { "redirectUri: ${loginProperties.lineRedirectUri}" }
        logger.info { "code: $code" }
        logger.info { "clientSecret: ${loginProperties.lineClientSecret}" }
        return try {
            val formData =
                buildString {
                    append("grant_type=authorization_code")
                    append("&code=$code")
                    append("&redirect_uri=${loginProperties.lineRedirectUri}")
                    append("&client_id=${loginProperties.lineClientId}")
                    append("&client_secret=${loginProperties.lineClientSecret}")
                }
            logger.info { "라인 토큰 요청을 위한 formData: $formData" }
            val response = lineAuthClient.getAccessToken(formData)
            logger.info { "라인으로부터 돌아온 토큰 responseDTO: $response" }
            response.idToken
        } catch (e: Exception) {
            logger.error(e) { "라인 Access Token 요청 실패" }
            throw e
        }
    }

    private fun decodeUserInfoFromToken(idToken: String): UserDetails {
        logger.info { "라인 ID 토큰으로 유저 정보 디코딩 시작" }

        return try {
            // JWT 토큰의 payload 부분 추출 및 디코딩
            val payload = idToken.split(".")[1]
            val decodedBytes =
                Base64
                    .getUrlDecoder()
                    .decode(payload.padEnd((payload.length + 3) / 4 * 4, '='))
            val decodedString = String(decodedBytes)
            logger.info { "디코딩된 페이로드 전체: $decodedString" }
            // JSON 파싱
            val jsonObject = ObjectMapper().readTree(decodedString)

            // id 토큰 제공자가 라인인지 확인
            if (jsonObject["iss"].asText() != "https://access.line.me"
            ) {
                logger.error { "line id token 제공자가 line이 아닌 에러" }
                throw CommonException(ErrorCode.LINE_AUTH_ERROR)
            }

            // sub 필드가 라인 회원 번호
            val lineId = jsonObject["sub"].asText()
            logger.info { "라인 제공 고유 id: $lineId" }

            // 이메일 생성
            val email = "$lineId@line.com"

            // 라인 payload 데이터 구성
            val lineResponse =
                LinePayloadDTO(
                    id = lineId,
                    name = jsonObject["name"]?.asText(),
                )

            logger.info { "디코딩된 라인 계정 유저 정보: $lineResponse" }

            // 기존 회원 조회 또는 새 회원 생성
            val member =
                memberRepository.findMemberByMemberEmail(email)
                    ?: createNewLineMember(email, lineResponse)
            if (member.memberStatus == MemberStatus.ROLE_BLACKLIST) {
                logger.warn { "라인 블랙 리스트 멤버가 로그인 - email: $email" }
                throw CommonException(ErrorCode.BLACK_LIST_ROLE)
            }
            val authority = listOf(SimpleGrantedAuthority(member.memberStatus.toString()))
            CustomMember(member, authority)
        } catch (e: Exception) {
            logger.error(e) { "라인 ID 토큰 디코딩 실패" }
            throw IllegalArgumentException("Invalid ID token", e)
        }
    }

    private fun createNewLineMember(
        email: String,
        lineResponse: LinePayloadDTO,
    ): Member {
        val newGoogleMember =
            Member(
                memberEmail = email,
                memberPhoneNumber = "FromLine",
                memberPhoto = "",
                memberStatus = MemberStatus.ROLE_TEMP,
                memberRegion = "",
                memberLanguage = Language.KOR,
                memberGender = Gender.UNKNOWN,
                memberNickname = lineResponse.name ?: UUID.randomUUID().toString(),
                memberBirth = LocalDate.now(),
                memberPassword = UUID.randomUUID().toString(),
                signupPath = SignUpPath.LINE,
                selfIntroduction = "",
            )

        logger.info { "새로 생성되는 라인 계정 멤버: $newGoogleMember" }
        memberRepository.save(newGoogleMember)
        logger.info { "라인 계정 새 멤버 저장 성공" }
        return newGoogleMember
    }
}