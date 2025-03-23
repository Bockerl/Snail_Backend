package com.bockerl.snailchat.infrastructure.event.consumer

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaMessageConsumerImpl(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) : KafkaMessageConsumer