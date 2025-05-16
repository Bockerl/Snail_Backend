package com.bockerl.snailmember.infrastructure.event.processor

import com.bockerl.snailmember.common.event.DomainFailEvent
import com.bockerl.snailmember.infrastructure.metrics.DomainFailMetrics
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.Materialized
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class DomainFailStreamProcessor(
    private val objectMapper: ObjectMapper,
) {
    @Bean
    fun domainFailStream(
        @Qualifier("streamBuilder") builder: StreamsBuilder,
        domainFailMetrics: DomainFailMetrics,
    ) {
        val stream =
            builder
                .stream<String, String>("domain-fail-events")
                .mapValues { values -> objectMapper.readValue(values, DomainFailEvent::class.java) }
        stream
            .groupBy({ _, value -> value.domainName }, Grouped.with(Serdes.String(), domainFailSerde()))
            .count(Materialized.`as`("domainType-count-store"))
            .toStream()
            .foreach { key, _ -> domainFailMetrics.incrementFailCounter(key) }
    }

    private fun domainFailSerde(): Serde<DomainFailEvent> =
        Serdes.serdeFrom(
            JsonSerializer(objectMapper),
            JsonDeserializer(DomainFailEvent::class.java, objectMapper),
        )
}