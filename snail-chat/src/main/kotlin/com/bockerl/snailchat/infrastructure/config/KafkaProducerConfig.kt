package com.bockerl.snailchat.infrastructure.config

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
@EnableKafka
class KafkaProducerConfig(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
) {
    // default 설정
    @Bean
    fun kafkaProducerFactory(): ProducerFactory<String, Any> {
        val configProps =
            mapOf<String, Any>(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
//                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true, // 멱등성 설정은 고민 필요
//                ProducerConfig.ACKS_CONFIG to "all", // 모든 래풀라카의 확인을 받는 것은 tradeoff 사항
//                ProducerConfig.RETRIES_CONFIG to 10, // 재시도 횟수 설정
//                ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION to 5, //  메시지 순서 보장(5가 넘어가면 순서 보장 못함)
//                ProducerConfig.COMPRESSION_TYPE_CONFIG to "snappy", // 압축 설정(gzip 같은 압축 프로세스, google에서 만듦)
            )
        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Any> = KafkaTemplate(kafkaProducerFactory())
}