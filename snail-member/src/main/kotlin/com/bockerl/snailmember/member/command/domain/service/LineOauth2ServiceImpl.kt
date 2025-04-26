package com.bockerl.snailmember.member.command.domain.service

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.infrastructure.config.TransactionalConfig
import com.bockerl.snailmember.infrastructure.outbox.dto.OutboxDTO
import com.bockerl.snailmember.infrastructure.outbox.enums.EventType
import com.bockerl.snailmember.infrastructure.outbox.service.OutboxService
import com.bockerl.snailmember.member.client.LineAuthClient
import com.bockerl.snailmember.member.command.application.dto.response.LinePayloadDTO
import com.bockerl.snailmember.member.command.application.dto.response.LoginResponseDTO
import com.bockerl.snailmember.member.command.application.service.LineOauth2Service
import com.bockerl.snailmember.member.command.config.Oauth2LoginProperties
import com.bockerl.snailmember.member.command.domain.aggregate.entity.Member
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.Gender
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.Language
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.MemberStatus
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.SignUpPath
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberCreateEvent
import com.bockerl.snailmember.member.command.domain.repository.MemberRepository
import com.bockerl.snailmember.security.CustomMember
import com.bockerl.snailmember.security.Oauth2JwtUtils
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.util.Base64
import java.util.UUID

@Service
class LineOauth2ServiceImpl(
    private val lineAuthClient: LineAuthClient,
    private val memberRepository: MemberRepository,
    private val loginProperties: Oauth2LoginProperties,
    private val jwtUtils: Oauth2JwtUtils,
    private val objectMapper: ObjectMapper,
    private val eventPublisher: ApplicationEventPublisher,
    private val outboxService: OutboxService,
) : LineOauth2Service {
    private val logger = KotlinLogging.logger {}

    override fun lineLogin(code: String): LoginResponseDTO =
        TransactionalConfig.run {
            // 코드 기반으로 요청을 보낸 뒤 id token만 추출
            val idToken = requestTokenFromLine(code)
            // 유저 정보 디코딩
            val customMember = decodeUserInfoFromToken(idToken) as CustomMember
            val response = jwtUtils.generateJwtResponse(customMember)
            return@run response
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
        val newLineMember =
            Member(
                memberEmail = email,
                memberPhoneNumber = UUID.randomUUID().toString(),
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

        logger.info { "새로 생성되는 라인 계정 멤버: $newLineMember" }
        memberRepository.save(newLineMember)
        logger.info { "라인 계정 새 멤버 저장 성공" }
        // outbox 이벤트 발행(회원 생성)
        val event =
            MemberCreateEvent(
                memberId = newLineMember.formattedId,
                timestamp = Instant.now(),
                memberEmail = newLineMember.memberEmail,
                memberPhoneNumber = newLineMember.memberPhoneNumber,
                memberStatus = newLineMember.memberStatus,
                memberRegion = newLineMember.memberRegion,
                memberGender = newLineMember.memberGender,
                memberNickname = newLineMember.memberNickname,
                memberPhoto = newLineMember.memberPhoto,
                memberBirth = newLineMember.memberBirth,
                memberLanguage = newLineMember.memberLanguage,
                signUpPath = SignUpPath.LINE,
            )
        // logging을 위한 비동기 리스너 이벤트 처리
        logger.info { "현재 Thread: ${Thread.currentThread().name}" }
        eventPublisher.publishEvent(event)
        val jsonPayLoad = objectMapper.writeValueAsString(event)
        val outBox =
            OutboxDTO(
                aggregateId = newLineMember.formattedId,
                eventType = EventType.MEMBER,
                payload = jsonPayLoad,
                // 회원가입은 1번만 발생하므로, pk를 멱등키로
                idempotencyKey = newLineMember.formattedId,
            )
        outboxService.createOutbox(outBox)
        return newLineMember
    }
}