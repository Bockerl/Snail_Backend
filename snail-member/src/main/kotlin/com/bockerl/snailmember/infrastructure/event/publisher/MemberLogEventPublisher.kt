package com.bockerl.snailmember.infrastructure.event.publisher

import com.bockerl.snailmember.common.event.BaseMemberEvent
import com.bockerl.snailmember.infrastructure.event.processor.DiscordNotifier
import com.bockerl.snailmember.infrastructure.outbox.enums.EventType
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class MemberLogEventPublisher(
    private val objectMapper: ObjectMapper,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val discordNotifier: DiscordNotifier,
) {
    val logger = KotlinLogging.logger {}

    // 새로운 thread에서 비동기 병렬처리
    @Async("logTaskExecutor")
    @EventListener
    fun logging(event: BaseMemberEvent) {
        val topic = EventType.MEMBER_LOGGING
        try {
            logger.info { "로그 이벤트 전송 시작: ${topic.topic}, 현재 Thread: ${Thread.currentThread().name}" }
            val payload = objectMapper.writeValueAsString(event)
            kafkaTemplate.send(topic.topic, payload)
            logger.info { "로그 이벤트 전송 완료: ${topic.topic}" }
        } catch (e: Exception) {
            logger.warn { "로그 이벤트 전송 실패: ${topic.topic}" }
            // discord webhook
            discordNotifier.notify(topic.topic, e.message)
        }
    }
}