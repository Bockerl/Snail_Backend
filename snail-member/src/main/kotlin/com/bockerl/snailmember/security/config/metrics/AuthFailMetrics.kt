package com.bockerl.snailmember.security.config.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class AuthFailMetrics(
    private val metricRegistry: MeterRegistry,
) {
    private val failCounter: MutableMap<String, Counter> = mutableMapOf()

    fun incrementFailCounter(failType: String) {
        val counter =
            failCounter.getOrPut(failType) {
                Counter
                    .builder("auth_fail_count")
                    .description("Count of authentication failures")
                    .tag("type", failType)
                    .register(metricRegistry)
            }
        counter.increment()
    }
}