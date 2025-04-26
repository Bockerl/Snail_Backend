package com.bockerl.snailmember.infrastructure.event.processor

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Component
class DiscordNotifier(
    @Value("\${DISCORD_WEBHOOK}")
    private val url: String,
) {
    private val logger = KotlinLogging.logger {}

    fun notify(
        topic: String,
        error: String?,
    ) {
        try {
            val payload =
                mapOf(
                    "username" to "LogNotifier",
                    "embeds" to
                        listOf(
                            mapOf(
                                "title" to "❌ Kafka 전송 실패",
                                "description" to "로그 이벤트 전송 중 예외 발생",
                                "color" to 16711680,
                                "fields" to
                                    listOf(
                                        mapOf("name" to "Topic", "value" to topic),
                                        mapOf("name" to "Error", "value" to (error ?: "Unknown")),
                                    ),
                            ),
                        ),
                )
            val body = ObjectMapper().writeValueAsString(payload)
            val client = HttpClient.newHttpClient()
            val request =
                HttpRequest
                    .newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build()
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        } catch (e: Exception) {
            logger.warn { "DiscordWebHook 전송 실패: ${e.message}" }
        }
    }
}