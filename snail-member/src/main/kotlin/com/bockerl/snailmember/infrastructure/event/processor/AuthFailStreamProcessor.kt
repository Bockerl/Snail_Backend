package com.bockerl.snailmember.infrastructure.event.processor

import com.bockerl.snailmember.infrastructure.metrics.AuthFailMetrics
import com.bockerl.snailmember.security.config.event.AuthFailEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Materialized
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class AuthFailStreamProcessor(
    private val objectMapper: ObjectMapper,
) {
    // 인증 실패 Stream Topology 설정(Kafka Streams의 데이터 처리 흐름을 정의한 실행 그래프)
    @Bean
    fun authFailStream(
        @Qualifier("streamBuilder") builder: StreamsBuilder,
        authFailMetrics: AuthFailMetrics,
    ): KStream<String, AuthFailEvent> {
        // Kafka 토픽 "auth-fail-log-events"에서 스트림을 가져옴
        val stream =
            builder
                .stream<String, String>("auth-fail-log-events")
                .mapValues { value -> objectMapper.readValue(value, AuthFailEvent::class.java) }
        // key를 failType으로
        stream
            // failType을 기준으로 Serde 생성 및 그룹화
            .groupBy({ _, value -> value.failType }, Grouped.with(Serdes.String(), authFailSerde()))
            // 상태 저장소(Materialized View) 생성
            // 내부 RocksDB에 "failType-count-store"라는 이름으로 count를 저장 (key: failType, value: Long)
            // Kafka Streams는 자동으로 changelog 토픽을 생성해서 durability 보장
            .count(Materialized.`as`("failType-count-store"))
            .toStream()
            .foreach { key, _ -> authFailMetrics.incrementFailCounter(key) }
        return stream
    }

    private fun authFailSerde(): Serde<AuthFailEvent> =
        Serdes.serdeFrom(
            JsonSerializer(objectMapper),
            JsonDeserializer(AuthFailEvent::class.java, objectMapper),
        )
}