package com.bockerl.snailchat.infrastructure.config

import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.KafkaAdmin

@Configuration
@Profile("dev") // 개발 단계에서 테스트를 위한 활성화
class KafkaTopicConfig(
    @Value("\${spring.kafka.bootstrap-servers}") val bootstrapAddress: String,
    @Value("\${spring.kafka.topic.personal-chat}") val personalChatTopic: String,
    @Value("\${spring.kafka.topic.group-chat}") val groupChatTopic: String,
) {
    @Bean
    fun kafkaAdmin(): KafkaAdmin {
        val configurations = mutableMapOf<String, Any>()
        configurations[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapAddress
        return KafkaAdmin(configurations)
    }

    @Bean
    fun personalChatTopic(): NewTopic {
        // 운영 환경에서는 각 토픽 데이터를 3개의 Broker에 복제하여 장애 발생 시 데이터 내구성을 확보합니다.
        return TopicBuilder
            .name(personalChatTopic)
            .partitions(1)
            .replicas(3)
            .build()
    }

    @Bean
    fun groupChatTopic(): NewTopic =
        TopicBuilder
            .name(groupChatTopic)
            .partitions(1)
            .replicas(3)
            .build()
}