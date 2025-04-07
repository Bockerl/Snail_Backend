package com.bockerl.snailmember.infrastructure.event.processor

import com.bockerl.snailmember.infrastructure.event.handler.MemberEventHandler
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberCreateEvent
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberDeleteEvent
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberLoginEvent
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberUpdateEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.TransientDataAccessException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class MemberEventProcessor(
    private val memberEventHandler: MemberEventHandler,
    private val dlqProcessor: DlqProcessor,
) {
    private val logger = KotlinLogging.logger {}

    // TransientDataAccessException: 일시적인 데이터 접근 문제 (예: 데이터베이스 연결 장애 등)가 발생할 때
    // DuplicateKeyException: 중복된 키로 인한 문제 (예: 이미 존재하는 값으로 insert 시도 등)로 인해 발생하는 예외
    @Retryable(
        value = [TransientDataAccessException::class, DuplicateKeyException::class],
        maxAttempts = 3,
        // delay: 첫 재시도 전 대기 시간. 여기서는 1000 밀리초(즉, 1초)를 의미
        // multiplier: 재시도마다 대기 시간을 지수적으로 증가(1sec, 2sec, 4sec...)
        // random: 설정한 delay 시간에 무작위 변동(jitter)을 추가(완전히 일정한 간격이 아님)
        backoff = Backoff(delay = 1000, multiplier = 2.0, random = true),
    )
    fun processCreate(
        event: MemberCreateEvent,
        eventId: String,
        idempotencyKey: String?,
    ) {
        logger.info { "멤버 생성 이벤트 처리 시작: $event" }
        memberEventHandler.handleCreate(event)
        logger.info { "멤버 생성 이벤트 처리 성공" }
    }

    @Retryable(
        value = [TransientDataAccessException::class, DuplicateKeyException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 1000, multiplier = 2.0, random = true),
    )
    fun processLogin(
        event: MemberLoginEvent,
        eventId: String,
        idempotencyKey: String?,
    ) {
        logger.info { "멤버 로그인 이벤트 처리 시작: $event" }
        memberEventHandler.handleLogin(event)
        logger.info { "멤버 로그인 이벤트 처리 성공" }
    }

    @Retryable(
        value = [TransientDataAccessException::class, DuplicateKeyException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 1000, multiplier = 2.0, random = true),
    )
    fun processUpdate(
        event: MemberUpdateEvent,
        eventId: String,
        idempotencyKey: String?,
    ) {
        logger.info { "멤버 수정 이벤트 처리 시작: $event" }
        memberEventHandler.handleUpdate(event)
        logger.info { "멤버 수정 이벤트 처리 성공" }
    }

    @Retryable(
        value = [TransientDataAccessException::class, DuplicateKeyException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 1000, multiplier = 2.0, random = true),
    )
    fun processDelete(
        event: MemberDeleteEvent,
        eventId: String,
        idempotencyKey: String?,
    ) {
        logger.info { "멤버 탈퇴 이벤트 처리 시작: $event" }
        memberEventHandler.handleDelete(event)
        logger.info { "멤버 틸퇴 이벤트 처리 성공" }
    }

    @Recover
    fun recoverCreate(
        ex: Exception,
        event: MemberCreateEvent,
    ) {
        logger.error { "멤버 생성 이벤트 처리 실패, exception: $ex" }
        dlqProcessor.sendToDlq(event)
    }

    @Recover
    fun recoverLogin(
        ex: Exception,
        event: MemberLoginEvent,
    ) {
        logger.error { "멤버 로그인 이벤트 처리 실패, exception: $ex" }
        dlqProcessor.sendToDlq(event)
    }

    @Recover
    fun recoverUpdate(
        ex: Exception,
        event: MemberUpdateEvent,
    ) {
        logger.error { "멤버 업데이트 이벤트 처리 실패, exception: $ex" }
        dlqProcessor.sendToDlq(event)
    }

    @Recover
    fun recoverDelete(
        ex: Exception,
        event: MemberDeleteEvent,
    ) {
        logger.error { "멤버 탈퇴 이벤트 처리 실패, exception: $ex" }
        dlqProcessor.sendToDlq(event)
    }
}