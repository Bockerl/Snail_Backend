package com.bockerl.snailmember.member.command.domain.service

import com.bockerl.snailmember.area.command.domain.aggregate.entity.ActivityArea
import com.bockerl.snailmember.area.command.domain.aggregate.entity.AreaType
import com.bockerl.snailmember.area.command.domain.aggregate.event.ActivityAreaUpdateEvent
import com.bockerl.snailmember.area.command.domain.repository.ActivityAreaRepository
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.file.command.application.dto.CommandFileDTO
import com.bockerl.snailmember.file.command.application.service.CommandFileService
import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType
import com.bockerl.snailmember.infrastructure.config.TransactionalConfig
import com.bockerl.snailmember.infrastructure.outbox.dto.OutboxDTO
import com.bockerl.snailmember.infrastructure.outbox.enums.EventType
import com.bockerl.snailmember.infrastructure.outbox.service.OutboxService
import com.bockerl.snailmember.member.command.application.dto.request.ActivityAreaRequestDTO
import com.bockerl.snailmember.member.command.application.dto.request.ProfileRequestDTO
import com.bockerl.snailmember.member.command.application.service.CommandMemberService
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberLoginEvent
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberUpdateEvent
import com.bockerl.snailmember.member.command.domain.repository.MemberRepository
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.time.LocalDateTime

@Service
class CommandMemberServiceImpl(
    private val memberRepository: MemberRepository,
    private val activityAreaRepository: ActivityAreaRepository,
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
            memberRepository.findMemberByMemberEmail(email)
                ?: throw CommonException(ErrorCode.NOT_FOUND_MEMBER)

        member.let {
            logger.info { "이메일 로그인 성공 후, 마지막 로그인 시간 업데이트 시작, email: $email" }
            member.apply {
                lastAccessTime = LocalDateTime.now()
            }
            memberRepository
                .runCatching {
                    memberRepository.save(member)
                    // kafka 이벤트 발행
                    val event =
                        MemberLoginEvent(
                            memberId = member.formattedId,
                            timestamp = Instant.now(),
                            ipAddress = ipAddress,
                            userAgent = userAgent,
                        )
                    // logging을 위한 비동기 리스너 이벤트 처리
                    logger.info { "현재 Thread: ${Thread.currentThread().name}" }
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
                    throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR, "로그인 시간 업데이트 실패 - email: $email")
                }
        }
    }

    @Transactional
    override fun postActivityArea(
        memberId: String,
        requestDTO: ActivityAreaRequestDTO,
        idempotencyKey: String,
    ) {
        logger.info { "활동지역 등록 서비스 도착" }
        val member =
            memberRepository.findMemberByMemberId(extractDigits(memberId))
                ?: throw CommonException(ErrorCode.NOT_FOUND_MEMBER)
        val foundedMemberId = member.memberId!!
        val newEmdId = extractDigits(requestDTO.primaryId)
        logger.info { "기존 주 활동지역부터 삭제" }
        activityAreaRepository
            .runCatching {
                deleteByMemberIdAndAreaType(foundedMemberId, AreaType.PRIMARY)
            }.onSuccess {
                logger.info { "기존 주 활동지역 삭제 성공" }
            }.onFailure {
                throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR, "기존 주 활동지역 삭제 실패")
            }

        logger.info { "새로운 주 활동지역 생성" }
        val primaryId = ActivityArea.ActivityId(foundedMemberId, newEmdId)
        val primaryArea =
            ActivityArea(
                primaryId,
                areaType = AreaType.PRIMARY,
            )
        activityAreaRepository
            .runCatching {
                save(primaryArea)
            }.onSuccess {
                logger.info { "새로운 주 활동지역 저장 성공" }
            }.onFailure {
                throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR, "새로운 주 활동지역 저장 실패")
            }

        requestDTO.workplaceId?.let {
            logger.info { "직장근처 활동지역 수정 시작" }
            activityAreaRepository
                .runCatching {
                    deleteByMemberIdAndAreaType(foundedMemberId, AreaType.WORKPLACE)
                }.onSuccess {
                    logger.info { "기존 직장근처 활동지역 삭제 성공" }
                }.onFailure {
                    throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR, "기존 직장근처 활동지역 삭제 실패")
                }
            logger.info { "새로운 직장근처 활동지역 생성" }
            val newEmdId2 = extractDigits(requestDTO.workplaceId)
            val workplaceId = ActivityArea.ActivityId(foundedMemberId, newEmdId2)
            val workplaceArea =
                ActivityArea(
                    workplaceId,
                    areaType = AreaType.WORKPLACE,
                )
            activityAreaRepository
                .runCatching {
                    save(workplaceArea)
                }.onSuccess {
                    logger.info { "새로운 직장근처 활동지역 저장 성공" }
                }.onFailure {
                    throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR, "새로운 직장근처 활동지역 저장 실패")
                }
        }
        // 아웃박스 생성
        val event =
            ActivityAreaUpdateEvent(
                memberId = member.formattedId,
                timeStamp = Instant.now(),
                primaryId = requestDTO.primaryId,
                workplaceId = requestDTO.workplaceId,
            )
        // logging을 위한 비동기 리스너 이벤트 처리
        logger.info { "현재 Thread: ${Thread.currentThread().name}" }
//        eventPublisher.publishEvent(event)
        val jsonPayLoad = objectMapper.writeValueAsString(event)
        val outBox =
            OutboxDTO(
                aggregateId = memberId,
                eventType = EventType.ACTIVITY_AREA,
                payload = jsonPayLoad,
            )
        outboxService.createOutbox(outBox)
    }

    override fun patchProfile(
        memberId: String,
        requestDTO: ProfileRequestDTO,
        file: MultipartFile?,
        idempotencyKey: String,
    ): Unit =
        TransactionalConfig.run {
            logger.info { "프로필 수정 서비스 도착" }
            val member =
                memberRepository.findMemberByMemberId(extractDigits(memberId))
                    ?: throw CommonException(ErrorCode.NOT_FOUND_MEMBER)
            member.apply {
                memberNickname = requestDTO.nickName
                memberBirth = requestDTO.birth
                memberGender = requestDTO.gender
                selfIntroduction = requestDTO.selfIntroduction
            }
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
            // 아웃박스 발행
            val event =
                MemberUpdateEvent(
                    memberId = memberId,
                    timestamp = Instant.now(),
                    memberNickname = member.memberNickname,
                    memberPhoneNumber = member.memberPhoneNumber,
                    memberBirth = member.memberBirth,
                    memberRegion = member.memberRegion,
                    memberGender = member.memberGender,
                    memberLanguage = member.memberLanguage,
                    memberStatus = member.memberStatus,
                )
            // logging을 위한 비동기 리스너 이벤트 처리
            logger.info { "현재 Thread: ${Thread.currentThread().name}" }
            eventPublisher.publishEvent(event)
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

    private fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()
}