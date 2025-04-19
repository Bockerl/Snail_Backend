package com.bockerl.snailchat.perf

interface PerfService {
    fun getIdempotencyKey(key: String): Boolean

    fun getIdempotencyKeyWithRedis(key: String): Boolean
}