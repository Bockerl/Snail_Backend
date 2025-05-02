package com.bockerl.snailmember.infrastructure.config

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.StreamsBuilderFactoryBean

@Configuration
class KafkaStreamConfig(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
) {
    @Bean
    fun authFailStreamConfig(): KafkaStreamsConfiguration {
        val props =
            mapOf(
                StreamsConfig.APPLICATION_ID_CONFIG to "auth-fail-streams-app",
                StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG to Serdes.String()::class.java,
                StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG to Serdes.String()::class.java,
            )
        return KafkaStreamsConfiguration(props)
    }

    @Bean
    fun authFailStreamBuilder(): StreamsBuilderFactoryBean = StreamsBuilderFactoryBean(authFailStreamConfig())
}