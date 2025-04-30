package com.bockerl.snailmember.member.command.domain.service

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.infrastructure.config.TransactionalConfig
import com.bockerl.snailmember.infrastructure.outbox.dto.OutboxDTO
import com.bockerl.snailmember.infrastructure.outbox.enums.EventType
import com.bockerl.snailmember.infrastructure.outbox.service.OutboxService
import com.bockerl.snailmember.member.client.GoogleAuthClient
import com.bockerl.snailmember.member.command.application.dto.response.GooglePayloadDTO
import com.bockerl.snailmember.member.command.application.dto.response.LoginResponseDTO
import com.bockerl.snailmember.member.command.application.service.GoogleOauth2Service
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
import java.time.LocalDate
import java.util.Base64
import java.util.UUID

@Service
class GoogleOauth2ServiceImpl(
    private val memberRepository: MemberRepository,
    private val loginProperties: Oauth2LoginProperties,
    private val googleAuthClient: GoogleAuthClient,
    private val jwtUtils: Oauth2JwtUtils,
    private val objectMapper: ObjectMapper,
    private val eventPublisher: ApplicationEventPublisher,
    private val outboxService: OutboxService,
) : GoogleOauth2Service {
    private val logger = KotlinLogging.logger {}

    override fun googleLogin(code: String): LoginResponseDTO =
        TransactionalConfig.run {
            // 코드 기반으로 요청을 보낸 뒤 id token만 추출
            val idToken = requestTokenFromGoogle(code)
            // 유저 정보 디코딩
            val customMember = decodeUserInfoFromToken(idToken) as CustomMember
            val response = jwtUtils.generateJwtResponse(customMember)
            return@run response
        }

    private fun requestTokenFromGoogle(code: String): String {
        logger.info { "구글 코드 기반 인증 토큰 요청 시작" }
        logger.info { "clientId: ${loginProperties.googleClientId}" }
        logger.info { "redirectUri: ${loginProperties.googleRedirectUri}" }
        logger.info { "code: $code" }
        logger.info { "clientSecret: ${loginProperties.googleClientSecret}" }
        return try {
            val response =
                googleAuthClient.getAccessToken(
                    clientId = loginProperties.googleClientId,
                    redirectUri = loginProperties.googleRedirectUri,
                    code = code,
                    clientSecret = loginProperties.googleClientSecret,
                )
            logger.info { "구글로부터 돌아온 토큰 responseDTO: $response" }
            response.idToken
        } catch (e: Exception) {
            logger.error(e) { "구글 Access Token 요청 실패" }
            throw e
        }
    }

    private fun decodeUserInfoFromToken(idToken: String): UserDetails {
        logger.info { "구글 ID 토큰으로 유저 정보 디코딩 시작" }

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

            // id 토큰 제공자가 구글인지 확인
            if (jsonObject["iss"].asText() != "https://accounts.google.com" &&
                jsonObject["iss"].asText() != "accounts.google.com"
            ) {
                logger.error { "구글 id token 제공자가 google이 아닌 에러" }
                throw CommonException(ErrorCode.GOOGLE_AUTH_ERROR)
            }

            // sub 필드가 구글 회원 번호
            val googleId = jsonObject["sub"].asText()
            logger.info { "구글 제공 고유 id: $googleId" }

            // 이메일 생성
            val email = "$googleId@google.com"

            // 구글 payload 데이터 구성
            val googleResponse =
                GooglePayloadDTO(
                    id = googleId,
                    name = jsonObject["name"]?.asText(),
                )

            logger.info { "디코딩된 구글 계정 유저 정보: $googleResponse" }

            // 기존 회원 조회 또는 새 회원 생성
            val member =
                memberRepository.findMemberByMemberEmail(email)
                    ?: createNewGoogleMember(email, googleResponse)
            if (member.memberStatus == MemberStatus.ROLE_BLACKLIST) {
                logger.warn { "구글 블랙 리스트 멤버가 로그인 - email: $email" }
                throw CommonException(ErrorCode.BLACK_LIST_ROLE)
            }
            val authority = listOf(SimpleGrantedAuthority(member.memberStatus.toString()))
            CustomMember(member, authority)
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "구글 ID 토큰 디코딩 실패" }
            throw IllegalArgumentException("Invalid ID token", e)
        } catch (e: CommonException) {
            throw e
        }
    }

    private fun createNewGoogleMember(
        email: String,
        googleResponse: GooglePayloadDTO,
    ): Member {
        val newGoogleMember =
            Member(
                memberEmail = email,
                memberPhoneNumber = UUID.randomUUID().toString(),
                memberPhoto = "",
                memberStatus = MemberStatus.ROLE_TEMP,
                memberRegion = "",
                memberLanguage = Language.KOR,
                memberGender = Gender.UNKNOWN,
                memberNickname = googleResponse.name ?: UUID.randomUUID().toString(),
                memberBirth = LocalDate.now(),
                memberPassword = UUID.randomUUID().toString(),
                signupPath = SignUpPath.Google,
                selfIntroduction = "",
            )

        logger.info { "새로 생성되는 구글 계정 멤버: $newGoogleMember" }
        memberRepository.save(newGoogleMember)
        logger.info { "구글 계정 새 멤버 저장 성공" }
        // outbox 이벤트 발행(회원 생성)
        val event =
            MemberCreateEvent(
                memberId = newGoogleMember.formattedId,
                memberEmail = newGoogleMember.memberEmail,
                memberPhoneNumber = newGoogleMember.memberPhoneNumber,
                memberStatus = newGoogleMember.memberStatus,
                memberRegion = newGoogleMember.memberRegion,
                memberGender = newGoogleMember.memberGender,
                memberNickname = newGoogleMember.memberNickname,
                memberPhoto = newGoogleMember.memberPhoto,
                memberBirth = newGoogleMember.memberBirth,
                memberLanguage = newGoogleMember.memberLanguage,
                signUpPath = SignUpPath.Google,
            )
        // logging을 위한 비동기 리스너 이벤트 처리
        logger.info { "현재 Thread: ${Thread.currentThread().name}" }
        eventPublisher.publishEvent(event)
        val jsonPayLoad = objectMapper.writeValueAsString(event)
        val outBox =
            OutboxDTO(
                aggregateId = newGoogleMember.formattedId,
                eventType = EventType.MEMBER,
                payload = jsonPayLoad,
                // 회원가입은 1번만 발생하므로, pk를 멱등키로
                idempotencyKey = newGoogleMember.formattedId,
            )
        outboxService.createOutbox(outBox)
        return newGoogleMember
    }
}