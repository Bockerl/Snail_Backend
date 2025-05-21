@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.infrastructure.outbox.dto.OutboxDTO
import com.bockerl.snailmember.infrastructure.outbox.service.OutboxService
import com.bockerl.snailmember.member.client.KaKaoAuthClient
import com.bockerl.snailmember.member.command.config.Oauth2LoginProperties
import com.bockerl.snailmember.member.command.domain.aggregate.entity.Member
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.MemberStatus
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
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

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

        Given("카카오 회원인 사용자가") {
            val code = TEST_CODE
            val dummyPayloadJson = """{"sub":"1234567890"}"""
            val encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(dummyPayloadJson.toByteArray())
            val dummyToken = "header.$encodedPayload.signature"
            val response = createKaKaoTokenResponseDTO(idToken = dummyToken)
            val memberSlot = slot<Member>()
            val eventSlot = slot<Any>()
            val outBoxSlot = slot<OutboxDTO>()
            every { loginProperties.kakaoClientId } returns TEST_CLIENT_ID
            every { loginProperties.kakaoRedirectUri } returns TEST_REDIRECT_URI
            every { loginProperties.kakaoClientSecret } returns TEST_CLIENT_SECRET
            every {
                kakaoAuthClient.getAccessToken(
                    clientId = TEST_CLIENT_ID,
                    clientSecret = TEST_CLIENT_SECRET,
                    redirectUri = TEST_REDIRECT_URI,
                    code = code,
                )
            } returns response
            every { eventPublisher.publishEvent(any()) } just runs
            every { outBoxService.createOutbox(any()) } just runs

            When("처음으로 카카오 Oauth 로그인을 하면") {
                every {
                    memberRepository.findMemberByMemberEmailAndMemberStatusNot(
                        any(),
                        MemberStatus.ROLE_DELETED,
                    )
                } returns null
                every { memberRepository.save(capture(memberSlot)) } answers {
                    memberSlot.captured.apply { memberId = 1L }
                }
                oauth2Service.kakaoLogin(code)

                Then("카카오 정보 기반 새 회원이 생성된다") {
                    verify(exactly = 1) {
                        memberRepository.findMemberByMemberEmailAndMemberStatusNot(
                            "1234567890@kakao.com",
                            MemberStatus.ROLE_DELETED,
                        )
                    }

                    verify(exactly = 1) {
                        memberRepository.save(any())
                    }
                    memberSlot.captured.memberEmail shouldBe "1234567890@kakao.com"
                }

                // event verify

                // outbox verify
            }
        }

        fun String.toLocalDateOrNow(): LocalDate =
            try {
                LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyyMMdd"))
            } catch (e: Exception) {
                LocalDate.now()
            }
    })