@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.member.command.domain.service

import com.bockerl.snailmember.member.client.KaKaoAuthClient
import com.bockerl.snailmember.member.command.application.dto.response.KaKaoPayloadDTO
import com.bockerl.snailmember.member.command.application.service.KaKaoOauth2Service
import com.bockerl.snailmember.member.command.config.Oauth2LoginProperties
import com.bockerl.snailmember.member.command.domain.aggregate.entity.*
import com.bockerl.snailmember.member.command.domain.repository.MemberRepository
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class KaKaoOauth2ServiceImpl(
    private val memberRepository: MemberRepository,
    private val loginProperties: Oauth2LoginProperties,
    private val kakaoAuthClient: KaKaoAuthClient,
    // 추후에 jwtToken 발행도 덧붙일 것
) : KaKaoOauth2Service {
    private val logger = KotlinLogging.logger {}

    @Transactional
    override fun kakaoLogin(code: String): String {
        // 코드 기반으로 인증 토큰 요청
        val idToken = requestTokenFromKaKao(code)
        // 유저 정보를 조회
        val userInfo = decodeUserInfoFromToken(idToken)
        return "일단 성공"
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

    private fun decodeUserInfoFromToken(idToken: String): Member {
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
            memberRepository.findMemberByMemberEmail(email)
                ?: createNewKaKaoMember(email, kakaoResponse)
        } catch (e: Exception) {
            logger.error(e) { "카카오 ID 토큰 디코딩 실패" }
            throw IllegalArgumentException("Invalid ID token", e)
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
            memberStatus = MemberStatus.USER,
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

    private fun String.toLocalDateOrNow(): LocalDate = try {
        LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyyMMdd"))
    } catch (e: Exception) {
        logger.warn { "생년월일 파싱 실패 (입력값: $this): ${e.message}" }
        LocalDate.now()
    }
}
