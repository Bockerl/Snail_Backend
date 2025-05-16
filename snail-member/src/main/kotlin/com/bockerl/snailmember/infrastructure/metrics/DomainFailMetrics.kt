package com.bockerl.snailmember.infrastructure.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class DomainFailMetrics(
    private val metricRegistry: MeterRegistry,
) {
    private val failCounter: MutableMap<String, Counter> = mutableMapOf()

    fun incrementFailCounter(domainType: String) {
        val counter =
            failCounter.getOrPut(domainType) {
                Counter
                    .builder("domain_fail_count")
                    .description("Count of domain request failures")
                    .tag("type", domainType)
                    .register(metricRegistry)
            }
        counter.increment()
    }
}