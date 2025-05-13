package com.bockerl.snailmember.member.command.domain.service

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.infrastructure.config.TransactionalConfig
import com.bockerl.snailmember.infrastructure.outbox.dto.OutboxDTO
import com.bockerl.snailmember.infrastructure.outbox.enums.EventType
import com.bockerl.snailmember.infrastructure.outbox.service.OutboxService
import com.bockerl.snailmember.member.client.KaKaoAuthClient
import com.bockerl.snailmember.member.command.application.dto.response.KaKaoPayloadDTO
import com.bockerl.snailmember.member.command.application.service.KaKaoOauth2Service
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
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.UUID

@Service
class KaKaoOauth2ServiceImpl(
    private val memberRepository: MemberRepository,
    private val loginProperties: Oauth2LoginProperties,
    private val kakaoAuthClient: KaKaoAuthClient,
    private val jwtUtils: Oauth2JwtUtils,
    private val objectMapper: ObjectMapper,
    private val eventPublisher: ApplicationEventPublisher,
    private val outboxService: OutboxService,
) : KaKaoOauth2Service {
    private val logger = KotlinLogging.logger {}

    override fun kakaoLogin(code: String) =
        TransactionalConfig.run {
            val idToken = requestTokenFromKaKao(code)
            val customMember = decodeUserInfoFromToken(idToken) as CustomMember
            jwtUtils.generateJwtResponse(customMember)
        }

    private fun requestTokenFromKaKao(code: String): String {
        logger.info { "카카오 코드 기반 인증 토큰 요청 시작" }
        logger.info { "clientId: ${loginProperties.kakaoClientId}" }
        logger.info { "redirectUri: ${loginProperties.kakaoRedirectUri}" }
        logger.info { "code: $code" }
        logger.info { "clientSecret: ${loginProperties.kakaoClientSecret}" }
        return try {
            val response =
                kakaoAuthClient.getAccessToken(
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

    private fun decodeUserInfoFromToken(idToken: String): UserDetails {
        logger.info { "카카오 ID 토큰으로 유저 정보 디코딩 시작" }
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
            // sub 필드가 카카오 회원 번호
            val kakaoId = jsonObject["sub"].asText()
            logger.info { "카카오 제공 고유 id: $kakaoId" }
            // 이메일 생성
            val email = "$kakaoId@kakao.com"
            // 카카오 응답 데이터 구성
            val kakaoResponse =
                KaKaoPayloadDTO(
                    id = kakaoId.toLong(),
                    email = email,
                    nickname = jsonObject["nickname"]?.asText(),
                    birthday = jsonObject["birthday"]?.asText(),
                    birthyear = jsonObject["birthyear"]?.asText(),
                )
            logger.info { "디코딩된 카카오 계정 유저 정보: $kakaoResponse" }
            // 기존 회원 조회 또는 새 회원 생성
            val member =
                memberRepository.findMemberByMemberEmailAndMemberStatusNot(email, MemberStatus.ROLE_DELETED)
                    ?: createNewKaKaoMember(email, kakaoResponse)
            if (member.memberStatus == MemberStatus.ROLE_BLACKLIST) {
                logger.warn { "카카오 블랙 리스트 멤버가 로그인 - email: $email" }
                throw CommonException(ErrorCode.BLACK_LIST_ROLE)
            }
            val authority = listOf(SimpleGrantedAuthority(member.memberStatus.toString()))
            CustomMember(member, authority)
        } catch (e: Exception) {
            logger.error(e) { "카카오 ID 토큰 디코딩 실패" }
            throw e
        }
    }

    private fun createNewKaKaoMember(
        email: String,
        kakaoResponse: KaKaoPayloadDTO,
    ): Member {
        val birth =
            if (!kakaoResponse.birthyear.isNullOrEmpty() && !kakaoResponse.birthday.isNullOrEmpty()) {
                "${kakaoResponse.birthyear}${kakaoResponse.birthday}".toLocalDateOrNow()
            } else {
                LocalDate.now()
            }

        val newKaKaoMember =
            Member(
                memberEmail = email,
                memberPhoneNumber = UUID.randomUUID().toString(),
                memberPhoto = "",
                memberStatus = MemberStatus.ROLE_TEMP,
                memberRegion = "",
                memberLanguage = Language.KOR,
                memberGender = Gender.UNKNOWN,
                memberNickname = kakaoResponse.nickname ?: UUID.randomUUID().toString(),
                memberBirth = birth,
                memberPassword = UUID.randomUUID().toString(),
                signupPath = SignUpPath.Kakao,
                selfIntroduction = "",
            )

        logger.info { "새로 생성되는 카카오 계정 멤버: $newKaKaoMember" }
        memberRepository
            .runCatching {
                save(newKaKaoMember)
            }.onSuccess {
                logger.info { "카카오 계정 새 멤버 저장 성공" }
            }.onFailure {
                logger.error { "카카오 계정 새 멤버 저장 실패, member: $newKaKaoMember, message: ${it.message}" }
                throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
            }.getOrThrow()

        // outbox 이벤트 발행(회원 생성)
        val event =
            MemberCreateEvent(
                memberId = newKaKaoMember.formattedId,
                memberEmail = newKaKaoMember.memberEmail,
                memberPhoneNumber = newKaKaoMember.memberPhoneNumber,
                memberStatus = newKaKaoMember.memberStatus,
                memberRegion = newKaKaoMember.memberRegion,
                memberGender = newKaKaoMember.memberGender,
                memberNickname = newKaKaoMember.memberNickname,
                memberPhoto = newKaKaoMember.memberPhoto,
                memberBirth = newKaKaoMember.memberBirth,
                memberLanguage = newKaKaoMember.memberLanguage,
                signUpPath = SignUpPath.Kakao,
            )
        // logging을 위한 비동기 리스너 이벤트 처리
        eventPublisher.publishEvent(event)

        val jsonPayLoad = objectMapper.writeValueAsString(event)
        val outBox =
            OutboxDTO(
                aggregateId = newKaKaoMember.formattedId,
                eventType = EventType.MEMBER,
                payload = jsonPayLoad,
                // 회원가입은 1번만 발생하므로, pk를 멱등키로
                idempotencyKey = newKaKaoMember.formattedId,
            )
        outboxService.createOutbox(outBox)
        return newKaKaoMember
    }

    private fun String.toLocalDateOrNow(): LocalDate =
        try {
            LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyyMMdd"))
        } catch (e: Exception) {
            logger.warn { "생년월일 파싱 실패 (입력값: $this): ${e.message}" }
            LocalDate.now()
        }
}