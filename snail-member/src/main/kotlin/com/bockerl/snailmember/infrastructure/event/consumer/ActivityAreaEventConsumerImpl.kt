package com.bockerl.snailmember.infrastructure.event.consumer

import com.bockerl.snailmember.area.command.domain.aggregate.event.ActivityAreaCreateEvent
import com.bockerl.snailmember.area.command.domain.aggregate.event.ActivityAreaUpdateEvent
import com.bockerl.snailmember.common.event.BaseActivityAreaEvent
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.infrastructure.event.processor.ActivityAreaEventProcessor
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ActivityAreaEventConsumerImpl(
    private val activityAreaEventProcessor: ActivityAreaEventProcessor,
) : ActivityAreaEventConsumer {
    private val logger = KotlinLogging.logger {}

    @Transactional
    @KafkaListener(
        topics = ["activity-area-events"],
        groupId = "snail-member",
        containerFactory = "kafkaListenerContainerFactory",
    )
    fun consumeActivityAreaEvents(
        @Payload event: BaseActivityAreaEvent,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header("eventId") eventId: String,
        @Header("idempotencyKey") idempotencyKey: String?,
        acknowledgment: Acknowledgment,
    ) {
        logger.info { "Received event: $event, eventId: $eventId" }
        try {
            when (event) {
                is ActivityAreaCreateEvent -> activityAreaEventProcessor.processCreate(event, eventId, idempotencyKey)
                is ActivityAreaUpdateEvent -> activityAreaEventProcessor.processUpdate(event, eventId, idempotencyKey)
                else -> logger.warn { "알 수 없는 event: $event, eventId: $eventId" }
            }
            acknowledgment.acknowledge()
        } catch (e: Exception) {
            logger.error { "활동지역 이벤트 에러 발생, event: $event, eventId: $eventId" }
            throw CommonException(ErrorCode.EVENT_CONSUMER_ERROR)
        }
    }
}