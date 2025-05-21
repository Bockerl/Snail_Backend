@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.infrastructure.event.publisher

import com.bockerl.snailmember.common.event.DomainFailEvent
import com.bockerl.snailmember.infrastructure.event.processor.DiscordNotifier
import com.bockerl.snailmember.infrastructure.outbox.enums.EventType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import org.springframework.kafka.core.KafkaTemplate
import java.util.concurrent.CompletableFuture
import kotlin.RuntimeException

class DomainFailLogEventPublisherTests :
    BehaviorSpec({
        val kafkaTemplate = mockk<KafkaTemplate<String, Any>>(relaxed = true)
        val discordNotifier = mockk<DiscordNotifier>(relaxed = true)

        // 실제 구현체
        lateinit var publisher: DomainFailLogEventPublisher

        beforeContainer {
            clearAllMocks(answers = false)
            publisher = DomainFailLogEventPublisher(kafkaTemplate, discordNotifier)
        }

        Given("도메인 요청이") {
            val event =
                DomainFailEvent(
                    domainName = "MEMBER",
                    methodName = "patchProfile",
                    message = "dummy message",
                    cause = "dummy cause",
                )
            val topicSlot = slot<String>()
            val keySlot = slot<String>()
            val eventSlot = slot<DomainFailEvent>()

            When("실패할 경우") {
                every { kafkaTemplate.send(capture(topicSlot), capture(keySlot), capture(eventSlot)) } answers {
                    val result = null
                    CompletableFuture.completedFuture(result)
                }
                publisher.domainFailLogging(event)

                Then("Kafka에 도메인 실패 로그 이벤트가 발행된다") {
                    val topic = topicSlot.captured
                    val key = keySlot.captured
                    val sendEvent = eventSlot.captured
                    verify(exactly = 1) {
                        kafkaTemplate.send(topic, key, sendEvent)
                    }
                    topic shouldNotBe null
                    topic shouldBe EventType.DOMAIN_FAILED.topic
                    key shouldNotBe null
                    key shouldBe "MEMBER:patchProfile"
                    sendEvent shouldNotBe null
                    sendEvent.domainName shouldBe "MEMBER"
                    sendEvent.methodName shouldBe "patchProfile"
                    sendEvent.message shouldBe "dummy message"
                    sendEvent.cause shouldBe "dummy cause"
                }
            }

            When("Kafka가 이벤트 발행 실패 시") {
                every {
                    kafkaTemplate.send(
                        capture(topicSlot),
                        any(),
                        any(),
                    )
                } throws RuntimeException("Kafka send failed")
                every { discordNotifier.notify(any(), any()) } just Runs
                publisher.domainFailLogging(event)

                Then("Discord에 알림이 간다") {
                    val topic = topicSlot.captured
                    verify(exactly = 1) {
                        discordNotifier.notify(topic, "Kafka send failed")
                    }
                }
            }
        }
    })