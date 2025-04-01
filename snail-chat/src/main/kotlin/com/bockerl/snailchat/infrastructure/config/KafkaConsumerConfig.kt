@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailchat.infrastructure.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
@EnableKafka
class KafkaConsumerConfig(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @Value("\${spring.kafka.consumer.group-id}") private val groupId: String,
) {
    @Bean
    fun kafkaConsumerFactory(): ConsumerFactory<String, Any> {
        val props =
            mapOf<String, Any>(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
                ConsumerConfig.GROUP_ID_CONFIG to groupId,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to true,
//                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false, // 수동 커밋을 위한 설정 (ACK/NACK)
//                ConsumerConfig.MAX_POLL_RECORDS_CONFIG to 500,
//                ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG to 30000,
            )

        val objectMapper = ObjectMapper().registerKotlinModule()
        val deserializer =
            JsonDeserializer(Any::class.java, objectMapper).apply {
                addTrustedPackages("*")
            }

        return DefaultKafkaConsumerFactory(props, StringDeserializer(), deserializer)
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Any> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, Any>()
        factory.consumerFactory = kafkaConsumerFactory()
        factory.setConcurrency(1) // consumer thread 수 default 3 or 5
//        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL // 수동 커밋 설정
//        factory.setCommonErrorHandler(errorHandler())
//        factory.setRecordMessageConverter(StringJsonMessageConverter())

        return factory
    }

//    @Bean
//    fun errorHandler(): DefaultErrorHandler {
//        val fixedBackOff = FixedBackOff(1000L, 3) // 1초 간격, 최대 3번 시도
//        val recoverer =
//            DeadLetterPublishingRecoverer(kafkaConsumerTemplate()) { record, exception ->
//                logger.error(
//                    exception,
//                ) { "DLT로 메시지 전송: 토픽 =${record.topic()}, 파티션=${record.partition()}, 오프셋=${record.offset()}, 예외=${exception.message}" }
//                org.apache.kafka.common
//                    .TopicPartition("${record.topic()}.DLT", record.partition())
//            }
//
//        return DefaultErrorHandler(recoverer, fixedBackOff)
//    }

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