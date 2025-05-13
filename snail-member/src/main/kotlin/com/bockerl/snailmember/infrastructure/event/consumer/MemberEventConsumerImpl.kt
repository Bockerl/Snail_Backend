package com.bockerl.snailmember.infrastructure.event.consumer

import com.bockerl.snailmember.common.event.BaseMemberEvent
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.infrastructure.event.processor.MemberEventProcessor
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberCreateEvent
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberDeleteEvent
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberUpdateEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberEventConsumerImpl(
    private val memberEventProcessor: MemberEventProcessor,
) : MemberEventConsumer {
    private val logger = KotlinLogging.logger {}

    @Transactional
    @KafkaListener(
        topics = ["member-events"],
        groupId = "snail-member",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun consumeMemberEvents(
        @Payload event: BaseMemberEvent,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header("eventId") eventId: String,
        @Header("idempotencyKey") idempotencyKey: String?,
        acknowledgment: Acknowledgment,
    ) {
        logger.info { "Received event: $event, eventId: $eventId" }
        if (idempotencyKey.isNullOrBlank()) {
            logger.error { "유효하지 않은 멱등키, $idempotencyKey, eventId: $eventId" }
            throw CommonException(ErrorCode.INVALID_IDEMPOTENCY)
        }
        try {
            when (event) {
                is MemberCreateEvent -> memberEventProcessor.processCreate(event, eventId, idempotencyKey)
//                is MemberLoginEvent -> memberEventProcessor.processLogin(event, eventId, idempotencyKey)
                is MemberUpdateEvent -> memberEventProcessor.processUpdate(event, eventId, idempotencyKey)
                is MemberDeleteEvent -> memberEventProcessor.processDelete(event, eventId, idempotencyKey)
                else -> logger.warn { "알 수 없는 event: $event, eventId: $eventId" }
            }
            // 수동 커밋
            acknowledgment.acknowledge()
        } catch (e: Exception) {
            // DLQ or 보상 Transaction 추가
            logger.error { "멤버 이벤트 에러 발생, event: $event, eventId: $eventId" }
            throw CommonException(ErrorCode.EVENT_CONSUMER_ERROR, "cause: ${e.message}")
        }
    }
}