package com.bockerl.snailmember.infrastructure.event.processor

import com.bockerl.snailmember.common.event.BaseLikeEvent
import com.bockerl.snailmember.infrastructure.event.handler.LikeEventHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.TransientDataAccessException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class LikeEventProcessor(
    private val likeEventHandler: LikeEventHandler,
    private val dlqProcessor: DlqProcessor,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * 재시도 정책을 적용하여 Like 이벤트를 처리합니다.
     * 최대 3회 재시도하며, 첫 재시도는 1초 후 시작하고 지수 백오프를 적용합니다.
     */
    @Retryable(
        value = [TransientDataAccessException::class, DuplicateKeyException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 1000, multiplier = 2.0, random = true),
    )
    fun process(
        event: BaseLikeEvent,
        eventId: String,
        idempotencyKey: String,
    ) {
        logger.info { "이벤트 처리 시작: $event" }
        likeEventHandler.handle(event, eventId, idempotencyKey)
        logger.info { "이벤트 처리 성공: $event" }
    }

    /**
     * 재시도 실패 후 호출되는 회복 메서드.
     * DLQ로 이벤트를 전송하고 추가 로깅을 수행합니다.
     */
    @Recover
    fun recover(
        ex: Exception,
        event: BaseLikeEvent,
    ) {
        logger.error(ex) { "이벤트 처리 실패 후 DLQ 전송: $event" }
        dlqProcessor.sendToDlq(event)
    }
}