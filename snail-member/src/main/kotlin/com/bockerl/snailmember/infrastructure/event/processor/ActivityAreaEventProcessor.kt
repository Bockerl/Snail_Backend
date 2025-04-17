package com.bockerl.snailmember.infrastructure.event.processor

import com.bockerl.snailmember.area.command.domain.aggregate.event.ActivityAreaCreateEvent
import com.bockerl.snailmember.area.command.domain.aggregate.event.ActivityAreaUpdateEvent
import com.bockerl.snailmember.infrastructure.event.handler.ActivityAreaEventHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.TransientDataAccessException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class ActivityAreaEventProcessor(
    private val activityAreaHandler: ActivityAreaEventHandler,
    private val dlqProcessor: DlqProcessor,
) {
    private val logger = KotlinLogging.logger {}

    @Retryable(
        value = [TransientDataAccessException::class, DuplicateKeyException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 1000, multiplier = 2.0, random = true),
    )
    fun processCreate(
        event: ActivityAreaCreateEvent,
        eventId: String,
        idempotencyKey: String?,
    ) {
        logger.info { "활동지역 생성 이벤트 처리 시작, event: $event, eventId: $eventId" }
        activityAreaHandler.handleCreate(event)
        logger.info { "활동지역 생성 이벤트 처리 성공 " }
    }

    @Retryable(
        value = [TransientDataAccessException::class, DuplicateKeyException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 1000, multiplier = 2.0, random = true),
    )
    fun processUpdate(
        event: ActivityAreaUpdateEvent,
        eventId: String,
        idempotencyKey: String?,
    ) {
        logger.info { "활동지역 수정 이벤트 처리 시작, event: $event, eventId: $eventId" }
        activityAreaHandler.handleUpdate(event)
        logger.info { "활동지역 수정 이벤트 처리 성공 " }
    }

    @Recover
    fun recoverCreate(
        ex: Exception,
        event: ActivityAreaCreateEvent,
    ) {
        logger.error { "활동지역 생성 이벤트 처리 실패, exception: $ex" }
        dlqProcessor.sendToDlq(event)
    }

    @Recover
    fun recoverUpdate(
        ex: Exception,
        event: ActivityAreaUpdateEvent,
    ) {
        logger.error { "활동지역 수정 이벤트 처리 실패, exception: $ex" }
        dlqProcessor.sendToDlq(event)
    }
}