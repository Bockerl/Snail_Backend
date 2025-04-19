package com.bockerl.snailchat.perf

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/perf")
class PerfController(
    private val perfService: PerfService,
) {
    @GetMapping("/idempotency/noRedis/{key}")
    fun getIdempotencyKey(
        @PathVariable key: String,
    ): Boolean {
        val result = perfService.getIdempotencyKey(key)

        return result
    }

    @GetMapping("/idempotency/Redis/{key}")
    fun getIdempotencyKeyWithRedis(
        @PathVariable key: String,
    ): Boolean {
        val result = perfService.getIdempotencyKeyWithRedis(key)

        return result
    }
}