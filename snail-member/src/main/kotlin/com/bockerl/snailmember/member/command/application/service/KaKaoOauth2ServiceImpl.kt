package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.member.client.KaKaoAuthClient
import com.bockerl.snailmember.member.client.KaKaoUserInfoClient
import com.bockerl.snailmember.member.command.application.dto.response.KaKaoUserInfoResponseDTO
import com.bockerl.snailmember.member.command.config.Oauth2LoginProperties
import com.bockerl.snailmember.member.command.domain.aggregate.entity.Member
import com.bockerl.snailmember.member.command.domain.repository.MemberRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class Oauth2ServiceImpl(
    private val memberRepository: MemberRepository,
    private val loginProperties: Oauth2LoginProperties,
    private val kakaoAuthClient: KaKaoAuthClient,
    private val kakaoUserInfoClient: KaKaoUserInfoClient,
    // 추후에 jwtToken 발행도 덧붙일 것
) : Oauth2Service {
    private val logger = KotlinLogging.logger {}

    // 카카오로부터 코드로 인증 토큰을 제공받고, 토큰으로 유저 정보를 조회하는 메서드
    override fun getKaKaoToken(code: String): String {
        logger.info { "카카오 로그인 시작 - code: $code" }
        // 코드 기반으로 인증 토큰 요청
        val accessToken = requestTokenFromKaKao(code)
        val userInfo = requestUserInfoFromKaKao(accessToken)
        return "일단 성공"
    }

    private fun requestUserInfoFromKaKao(accessToken: String): Member {
        logger.info { "카카오 인증 토큰 기반 회원 정보 요청 시작" }
        return kakaoUserInfoClient.getUserInfo("Bearer $accessToken")
            .also { response ->
                logger.info { "카카오 계정 유저 정보: $response" }
            }
            .let { response ->
                val (id, _, _) = response
                logger.info { "카카오 제공 고유 id(Long): $id" }
                val email = "$id@kakao.com"
                Triple(email, response, memberRepository.findMemberByMemberEmail(email))
            }
            .let { (email, response, existingMember) ->
                existingMember ?: createNewKakaoMember(response)
            }
    }

    private fun createNewKakaoMember(response: KaKaoUserInfoResponseDTO): Member {
        TODO("Not yet implemented")
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
            response.accessToken
        } catch (e: Exception) {
            logger.error(e) { "카카오 Access Token 요청 실패" }
            throw e
        }
    }
}
