@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.config

import com.bockerl.snailmember.boardlike.command.domain.aggregate.event.BoardLikeEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.util.backoff.FixedBackOff

@Configuration
class KafkaConsumerConfig(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @Value("\${spring.kafka.consumer.group-id}") private val groupId: String,
) {
    private val logger = KotlinLogging.logger {}

    @Bean
    fun kafkaConsumerFactory(): ConsumerFactory<String, BoardLikeEvent> {
        val props =
            mapOf<String, Any>(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
                ConsumerConfig.GROUP_ID_CONFIG to groupId,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
                ConsumerConfig.MAX_POLL_RECORDS_CONFIG to 500,
                ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG to 30000,
            )

        val deserializer =
            JsonDeserializer(BoardLikeEvent::class.java).apply {
                // 설명. 각각의 service를 추가해야 하지 않을까 싶습니다
                addTrustedPackages("*")
            }
        return DefaultKafkaConsumerFactory(props, StringDeserializer(), deserializer)
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, BoardLikeEvent> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, BoardLikeEvent>()
        factory.consumerFactory = kafkaConsumerFactory()
        // 설명. consumer thread 수 default 3 or 5
        factory.setConcurrency(1)
        // 설명. 수동 커밋 설정
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL

        // 설명. 에러 핸들러 설정
        factory.setCommonErrorHandler(errorHandler())

        return factory
    }

    // 설명. 필터링하는 listenerContainerFactory 설정 가능

    // 설명. error handler를 통해서 실패 메시지를 처리할 dlt
    @Bean
    fun errorHandler(): DefaultErrorHandler {
        // 설명. 1초 간격, 최대 3번 재시도
        val fixedBackOff = FixedBackOff(1000L, 3)

        // 설명. Dead Letter Queue(DLQ)로 전송하는 Recoverer 설정
        val recoverer =
            DeadLetterPublishingRecoverer(kafkaConsumerTemplate()) { record, exception ->
                logger.error(
                    exception,
                ) { "DLT로 메시지 전송: 토픽 =${record.topic()}, 파티션=${record.partition()}, 오프셋=${record.offset()}, 예외=${exception.message}" }
                org.apache.kafka.common
                    .TopicPartition("${record.topic()}.DLT", record.partition())
            }

        return DefaultErrorHandler(recoverer, fixedBackOff)
    }

    @Bean
    fun kafkaConsumerTemplate(): KafkaTemplate<String, Any> = KafkaTemplate(producerFactory())

    @Bean
    fun producerFactory(): ProducerFactory<String, Any> {
        val props =
            mapOf<String, Any>(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true,
            )
        return DefaultKafkaProducerFactory(props)
    }
}