package com.bockerl.snailmember.area.command.domain.service

import com.bockerl.snailmember.area.command.application.service.CommandAreaService
import com.bockerl.snailmember.area.command.domain.aggregate.entity.ActivityArea
import com.bockerl.snailmember.area.command.domain.aggregate.entity.AreaType
import com.bockerl.snailmember.area.command.domain.aggregate.event.ActivityAreaUpdateEvent
import com.bockerl.snailmember.area.command.domain.repository.ActivityAreaRepository
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.infrastructure.outbox.dto.OutboxDTO
import com.bockerl.snailmember.infrastructure.outbox.enums.EventType
import com.bockerl.snailmember.infrastructure.outbox.service.OutboxService
import com.bockerl.snailmember.member.command.application.dto.request.ActivityAreaRequestDTO
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommandAreaServiceImpl(
    private val areaRepository: ActivityAreaRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val outboxService: OutboxService,
    private val objectMapper: ObjectMapper,
) : CommandAreaService {
    private val logger = KotlinLogging.logger {}

    @Transactional
    override fun postActivityArea(
        memberId: String,
        requestDTO: ActivityAreaRequestDTO,
        idempotencyKey: String,
    ) {
        logger.info { "활동지역 등록/수정 서비스 도착" }
        val extractedId = extractDigits(memberId)
        logger.info { "추출된 memberId: $memberId" }
        // 이벤트 생성
        val event =
            ActivityAreaUpdateEvent(
                memberId = memberId,
                primaryId = requestDTO.primaryId,
                workplaceId = requestDTO.workplaceId,
            )
        // logging을 위한 비동기 리스너 이벤트 처리
        eventPublisher.publishEvent(event)
        val newEmdId = extractDigits(requestDTO.primaryId)
        logger.info { "기존 주 활동지역부터 삭제" }
        areaRepository
            .runCatching {
                deleteByMemberIdAndAreaType(extractedId, AreaType.PRIMARY)
            }.onSuccess {
                logger.info { "기존 주 활동지역 삭제 성공" }
            }.onFailure {
                throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR, "기존 주 활동지역 삭제 실패")
            }.getOrThrow()
        logger.info { "새로운 주 활동지역 생성" }
        val primaryId = ActivityArea.ActivityId(extractedId, newEmdId)
        val primaryArea =
            ActivityArea(
                primaryId,
                areaType = AreaType.PRIMARY,
            )
        areaRepository
            .runCatching {
                save(primaryArea)
            }.onSuccess {
                logger.info { "새로운 주 활동지역 저장 성공" }
            }.onFailure {
                throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR, "새로운 주 활동지역 저장 실패")
            }.getOrThrow()
        requestDTO.workplaceId?.let {
            logger.info { "직장근처 활동지역 수정 시작" }
            areaRepository
                .runCatching {
                    deleteByMemberIdAndAreaType(extractedId, AreaType.WORKPLACE)
                }.onSuccess {
                    logger.info { "기존 직장근처 활동지역 삭제 성공" }
                }.onFailure {
                    throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR, "기존 Workplace 활동 지역 삭제 실패")
                }.getOrThrow()
            logger.info { "새로운 직장근처 활동지역 생성" }
            val newEmdId2 = extractDigits(requestDTO.workplaceId)
            val workplaceId = ActivityArea.ActivityId(extractedId, newEmdId2)
            val workplaceArea =
                ActivityArea(
                    workplaceId,
                    areaType = AreaType.WORKPLACE,
                )
            areaRepository
                .runCatching {
                    save(workplaceArea)
                }.onSuccess {
                    logger.info { "Workplace 활동 지역 저장 성공" }
                }.onFailure {
                    throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR, "Workplace 활동 지역 저장 실패")
                }.getOrThrow()
        }
        val jsonPayLoad = objectMapper.writeValueAsString(event)
        val outBox =
            OutboxDTO(
                aggregateId = memberId,
                eventType = EventType.ACTIVITY_AREA,
                payload = jsonPayLoad,
            )
        outboxService.createOutbox(outBox)
    }

    @Transactional
    override fun deleteActivityArea(
        memberId: String,
        idempotencyKey: String,
    ) {
        logger.info { "활동 지역 삭제 서비스 도착" }
        areaRepository
            .runCatching {
                logger.info { "Primary 활동 지역 삭제 시작" }
                deleteByMemberIdAndAreaType(extractDigits(memberId), AreaType.PRIMARY)
            }.onSuccess {
                logger.info { "Primary 활동 지역 삭제 성공, memberId: $memberId" }
            }.onFailure {
                // 로그 전송 및 보상 트랜잭션 예상
                throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR, "Primary 활동 지역 삭제 실패")
            }.getOrThrow()
        areaRepository
            .runCatching {
                logger.info { "WorkPlace 활동 지역 삭제 시작" }
                deleteByMemberIdAndAreaType(extractDigits(memberId), AreaType.WORKPLACE)
            }.onSuccess {
                logger.info { "Workplace 활동 지역 삭제 성공, memberId: $memberId" }
            }.onFailure {
                // 로그 전송 및 보상 트랜잭션 예상
                throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR, "Workplace 활동 지역 삭제 실패")
            }.getOrThrow()
    }

    private fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()
}