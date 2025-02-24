@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.config.TestConfiguration
import com.bockerl.snailmember.config.TestSupport
import com.bockerl.snailmember.member.client.KaKaoAuthClient
import com.bockerl.snailmember.member.command.config.Oauth2LoginProperties
import com.bockerl.snailmember.member.command.domain.repository.MemberRepository
import com.bockerl.snailmember.member.command.domain.service.KaKaoOauth2ServiceImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@ExtendWith(MockitoExtension::class)
@Import(TestConfiguration::class)
class KaKaoOauth2ServiceImplTests : TestSupport() {
    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var loginProperties: Oauth2LoginProperties

    @Mock
    private lateinit var kaKaoAuthClient: KaKaoAuthClient

    private lateinit var kakaoOauth2Service: KaKaoOauth2Service

    @BeforeEach
    fun setup() {
        kakaoOauth2Service = KaKaoOauth2ServiceImpl(
            memberRepository = memberRepository,
            loginProperties = loginProperties,
            kakaoAuthClient = kaKaoAuthClient,
        )
    }

    @Nested
    @DisplayName("카카오 로그인 관련 테스트")
    inner class KaKaoLogin {
//        @Test
//        @DisplayName("신규 회원의 카카오 로그인")
//        fun kaKaoLogin_NewMember() {
//            // given
//            val code = "test-code"
//            val accessToken = "test-access-token"
//            val kaKaoId = 12345L
//            val nickname = "testNickname"
//            val birthYear = "2020"
//            val birthDay = "1121"
//
//            whenever(
//                kaKaoAuthClient.getAccessToken(
//                    code = code,
//                    redirectUri = loginProperties.kakaoRedirectUri,
//                    clientId = loginProperties.kakaoClientId,
//                    clientSecret = loginProperties.kakaoClientSecret,
//                ),
//            ).thenReturn(
//                KaKaoTokenResponseDTO(
//                    tokenType = "",
//                    accessToken = accessToken,
//                    expiresIn = 0,
//                    refreshToken = "",
//                    refreshTokenExpiresIn = 0,
//                ),
//            )
//            whenever(kakaoUserInfoClient.getUserInfo("Bearer $accessToken")).thenReturn(
//                KaKaoUserInfoResponseDTO(
//                    id = kaKaoId,
//                    connectedAt = "",
//                    kakaoAccount = KaKaoAccount(
//                        profileNicknameNeedsAgreement = true,
//                        hasBirthday = true,
//                        hasBirthyear = true,
//                        birthyear = birthYear,
//                        birthday = birthDay,
//                        profile = Profile(
//                            nickname = nickname,
//                            isDefaultNickname = true,
//                        ),
//                    ),
//                ),
//            )
//            whenever(memberRepository.findMemberByMemberEmail("$kaKaoId@kakao.com")).thenReturn(null)
//            // when
//            kakaoOauth2Service.kakaoLogin(code)
//            // then
//            verify(memberRepository).findMemberByMemberEmail("$kaKaoId@kakao.com")
//            verify(memberRepository).save(
//                argThat { newMember ->
//                    newMember.memberEmail == "$kaKaoId@kakao.com" &&
//                        newMember.memberPhoneNumber == "FromKaKao" &&
//                        newMember.memberNickName == nickname &&
//                        newMember.memberStatus == MemberStatus.USER &&
//                        newMember.memberBirth == "$birthYear$birthDay".toLocalDateOrNow()
//                },
//            )
//        }
    }

    private fun String.toLocalDateOrNow(): LocalDate = try {
        LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyyMMdd"))
    } catch (e: Exception) {
        LocalDate.now()
    }
}
