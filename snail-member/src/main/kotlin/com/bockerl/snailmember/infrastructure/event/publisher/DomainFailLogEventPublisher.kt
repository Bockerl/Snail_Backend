package com.bockerl.snailmember.infrastructure.event.publisher

import com.bockerl.snailmember.common.event.DomainFailEvent
import com.bockerl.snailmember.infrastructure.event.processor.DiscordNotifier
import com.bockerl.snailmember.infrastructure.outbox.enums.EventType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class DomainFailLogEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val discordNotifier: DiscordNotifier,
) {
    private val logger = KotlinLogging.logger {}

    @Async("logTaskExecutor")
    @EventListener
    fun domainFailLogging(event: DomainFailEvent) {
        val topic = EventType.DOMAIN_FAILED
        // key = 순서 보장 단위 -> domain + method별
        val key = "${event.domainName}:${event.methodName}"
        try {
            logger.info { "도메인 실패 로그 이벤트 전송 시작: $topic" }
            kafkaTemplate.send(topic.topic, key, event)
            logger.info { "도메인 실패 로그 이벤트 전송 완료: $topic" }
        } catch (e: Exception) {
            logger.warn { "도메인 실패 로그 이벤트 전송 실패, 예외 메세지: ${e.message}" }
            discordNotifier.notify(topic.topic, e.message)
        }
    }
}