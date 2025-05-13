@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.member.command.domain.service

import com.bockerl.snailmember.utils.*

class KaKaoOauth2ServiceImplTests
//    :
//    BehaviorSpec({
//        // mock
//        val loginProperties = mockk<Oauth2LoginProperties>()
//        val memberRepository = mockk<MemberRepository>()
//        val kakaoAuthClient = mockk<KaKaoAuthClient>()
//        val jwtUtls = mockk<Oauth2JwtUtils>()
//        // 테스트 구현체
//        val kakaoOauth2Service =
//            KaKaoOauth2ServiceImpl(
//                memberRepository = memberRepository,
//                kakaoAuthClient = kakaoAuthClient,
//                loginProperties = loginProperties,
//                jwtUtils = jwtUtls,
//            )
//
//        Given("카카오 로그인을 요청한 사용자가") {
//            val code = TEST_CODE
//            val response = createKaKaoTokenResponseDTO()
//            every {
//                kakaoAuthClient.getAccessToken(
//                    clientId = TEST_CLIENT_ID,
//                    clientSecret = TEST_CLIENT_SECRET,
//                    redirectUri = TEST_REDIRECT_URI,
//                    code = code,
//                )
//            } returns response
//        }
//
//        fun String.toLocalDateOrNow(): LocalDate =
//            try {
//                LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyyMMdd"))
//            } catch (e: Exception) {
//                LocalDate.now()
//            }
//    })