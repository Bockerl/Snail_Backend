@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.member.client.KaKaoAuthClient
import com.bockerl.snailmember.member.command.config.Oauth2LoginProperties
import com.bockerl.snailmember.member.command.domain.repository.MemberRepository
import com.bockerl.snailmember.utils.*
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.mockk
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class KaKaoOauth2ServiceImplTests :
    BehaviorSpec({
        // mock
        val loginProperties = mockk<Oauth2LoginProperties>()
        val memberRepository = mockk<MemberRepository>()
        val kakaoAuthClient = mockk<KaKaoAuthClient>()
        // 테스트 구현체
//        val kakaoOauth2Service =
//            KaKaoOauthServiceImpl(
//                memberRepository = memberRepository,
//                kakaoAuthClient = kakaoAuthClient,
//                loginProperties = loginProperties,
//            )

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

        fun String.toLocalDateOrNow(): LocalDate =
            try {
                LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyyMMdd"))
            } catch (e: Exception) {
                LocalDate.now()
            }
    })