package com.bockerl.snailmember.member.command.domain.service

import com.bockerl.snailmember.area.command.application.service.CommandAreaService
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.file.command.application.dto.CommandFileDTO
import com.bockerl.snailmember.file.command.application.service.CommandFileService
import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType
import com.bockerl.snailmember.infrastructure.outbox.dto.OutboxDTO
import com.bockerl.snailmember.infrastructure.outbox.enums.EventType
import com.bockerl.snailmember.infrastructure.outbox.service.OutboxService
import com.bockerl.snailmember.member.command.application.dto.request.ProfileRequestDTO
import com.bockerl.snailmember.member.command.application.service.CommandMemberService
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.MemberStatus
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberDeleteEvent
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberLoginEvent
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberUpdateEvent
import com.bockerl.snailmember.member.command.domain.repository.MemberRepository
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
class CommandMemberServiceImpl(
    private val memberRepository: MemberRepository,
    private val activityAreaService: CommandAreaService,
    private val commandFileService: CommandFileService,
    private val outboxService: OutboxService,
    private val eventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper,
) : CommandMemberService {
    private val logger = KotlinLogging.logger {}

    @Transactional
    override fun putLastAccessTime(
        email: String,
        ipAddress: String,
        userAgent: String,
        idempotencyKey: String,
    ) {
        logger.info { "마지막 접속 시간 업데이트 서비스 도착" }
        val member =
            memberRepository.findMemberByMemberEmailAndMemberStatusNot(email, MemberStatus.ROLE_DELETED)
                ?: throw CommonException(ErrorCode.NOT_FOUND_MEMBER)
        member.lastAccessTime = LocalDateTime.now()
        memberRepository
            .runCatching {
                memberRepository.save(member)
                val event =
                    MemberLoginEvent(
                        memberId = member.formattedId,
                        userAgent = userAgent,
                        ipAddress = ipAddress,
                    )
                eventPublisher.publishEvent(event)
                // outBox를 통한 이벤트 처리
//                    val jsonPayload = objectMapper.writeValueAsString(event)
//                    val outBox =
//                        OutboxDTO(
//                            aggregateId = member.formattedId,
//                            eventType = EventType.MEMBER,
//                            payload = jsonPayload,
//                            idempotencyKey = idempotencyKey,
//                        )
//                    outboxService.createOutbox(outBox)
            }.onSuccess {
                logger.info { "마지막 로그인 시간 업데이트 성공 - email: $email" }
            }.onFailure {
                logger.info { "마지막 로그인 시간 업데이트 실패, memberId: ${member.formattedId}" }
                throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR, "로그인 시간 업데이트 실패 - email: $email")
            }
    }

    @Transactional
    override fun patchProfile(
        memberId: String,
        requestDTO: ProfileRequestDTO,
        file: MultipartFile?,
        idempotencyKey: String,
    ) {
        logger.info { "프로필 수정 서비스 도착" }
        val member =
            memberRepository.findMemberByMemberIdAndMemberStatusNot(extractDigits(memberId), MemberStatus.ROLE_DELETED)
                ?: throw CommonException(ErrorCode.NOT_FOUND_MEMBER)
        member.apply {
            memberNickname = requestDTO.nickName
            memberBirth = requestDTO.birth
            memberGender = requestDTO.gender
            selfIntroduction = requestDTO.selfIntroduction
        }
        // 이벤트 발행
        val event =
            MemberUpdateEvent(
                memberId = memberId,
                memberNickname = member.memberNickname,
                memberPhoneNumber = member.memberPhoneNumber,
                memberBirth = member.memberBirth,
                memberRegion = member.memberRegion,
                memberGender = member.memberGender,
                memberLanguage = member.memberLanguage,
                memberStatus = member.memberStatus,
            )
        // logging을 위한 비동기 리스너 이벤트 처리
        eventPublisher.publishEvent(event)
        memberRepository
            .runCatching {
                save(member)
            }.onSuccess {
                logger.info { "프로필 사진 제외 회원 정보 수정 성공" }
            }.onFailure {
                throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR, "프로플 사진 제외 회원 정보 수정 실패")
            }
        file?.let {
            logger.info { "프로필 사진 수정 시작" }
            val commandFileDTO =
                CommandFileDTO(
                    fileTargetType = FileTargetType.MEMBER,
                    fileTargetId = memberId,
                    memberId = memberId,
                    idempotencyKey = idempotencyKey,
                )
            // 기존 프사가 없다면 생성, 있다면 삭제 후 수정 호출
            if (member.memberPhoto.isBlank()) {
                val fileUrl = commandFileService.createSingleFile(file, commandFileDTO)
                logger.info { "프로필 사진 저장 성공 - fileUrl: $fileUrl" }
                member.apply { memberPhoto = fileUrl }
            } else {
                val fileUrl = commandFileService.updateProfileImage(file, commandFileDTO)
                logger.info { "프로필 사진 저장 성공 - fileUrl: $fileUrl" }
                member.apply { memberPhoto = fileUrl }
            }
            memberRepository
                .runCatching {
                    save(member)
                }.onSuccess {
                    logger.info { "프로필 사진 수정 성공" }
                }.onFailure {
                    throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR, "프로필 사진 수정 실패")
                }
        }
        val jsonPayLoad = objectMapper.writeValueAsString(event)
        val outBox =
            OutboxDTO(
                memberId,
                EventType.MEMBER,
                jsonPayLoad,
                idempotencyKey,
            )
        outboxService.createOutbox(outBox)
    }

    @Transactional
    override fun deleteMember(
        memberId: String,
        idempotencyKey: String,
    ) {
        logger.info { "멤버 탈퇴 서비스 도착" }
        val member =
            memberRepository
                .findMemberByMemberIdAndMemberStatusNot(extractDigits(memberId), MemberStatus.ROLE_DELETED)
                ?: throw CommonException(ErrorCode.NOT_FOUND_MEMBER)
        val event =
            MemberDeleteEvent(
                memberId = memberId,
                memberEmail = member.memberEmail,
            )
        eventPublisher.publishEvent(event)
        member.memberStatus = MemberStatus.ROLE_DELETED
        memberRepository
            .runCatching {
                logger.info { "멤버 탈퇴 시작" }
                save(member)
            }.onSuccess {
                logger.info { "멤버 탈퇴 성공, memberId: $memberId" }
            }.onFailure {
                logger.info { "멤버 탈퇴 실패, memberId: $memberId" }
                // 로그 전송 및 보상 트랜잭션 예상
                throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
            }
        // 회원 삭제 이벤트 아웃박스 생성
        val jsonPayLoad = objectMapper.writeValueAsString(event)
        val outBox =
            OutboxDTO(
                aggregateId = memberId,
                eventType = EventType.MEMBER,
                payload = jsonPayLoad,
                idempotencyKey = idempotencyKey,
            )
        outboxService.createOutbox(outBox)
    }

    private fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()
}