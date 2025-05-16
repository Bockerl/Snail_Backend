@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.infrastructure.outbox.service.OutboxService
import com.bockerl.snailmember.member.client.KaKaoAuthClient
import com.bockerl.snailmember.member.command.config.Oauth2LoginProperties
import com.bockerl.snailmember.member.command.domain.repository.MemberRepository
import com.bockerl.snailmember.member.command.domain.service.KaKaoOauthServiceImpl
import com.bockerl.snailmember.security.Oauth2JwtUtils
import com.bockerl.snailmember.utils.*
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class KaKaoOauth2ServiceImplTests :
    BehaviorSpec({
        // mock
        val loginProperties = mockk<Oauth2LoginProperties>()
        val memberRepository = mockk<MemberRepository>()
        val kakaoAuthClient = mockk<KaKaoAuthClient>()
        val jwtUtils = mockk<Oauth2JwtUtils>()
        val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
        val outBoxService = mockk<OutboxService>(relaxed = true)
        val objectMapper =
            ObjectMapper().apply {
                registerModule(KotlinModule.Builder().build())
                registerModule(JavaTimeModule())
                enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }

        // 테스트 구현체
        lateinit var oauth2Service: KaKaoOauth2Service

        beforeContainer {
            clearAllMocks(answers = false)
            oauth2Service =
                KaKaoOauthServiceImpl(
                    memberRepository = memberRepository,
                    kakaoAuthClient = kakaoAuthClient,
                    loginProperties = loginProperties,
                    jwtUtils = jwtUtils,
                    eventPublisher = eventPublisher,
                    outboxService = outBoxService,
                    objectMapper = objectMapper,
                )
        }

        Given("카카오 로그인을 요청한 사용자가") {
            val code = TEST_CODE
            val response = createKaKaoTokenResponseDTO()
            every {
                kakaoAuthClient.getAccessToken(
                    clientId = TEST_CLIENT_ID,
                    clientSecret = TEST_CLIENT_SECRET,
                    redirectUri = TEST_REDIRECT_URI,
                    code = code,
                )
            } returns response
        }

        fun String.toLocalDateOrNow(): LocalDate =
            try {
                LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyyMMdd"))
            } catch (e: Exception) {
                LocalDate.now()
            }
    })