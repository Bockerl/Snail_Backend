@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.infrastructure.event.publisher

import com.bockerl.snailmember.common.event.BaseMemberEvent
import com.bockerl.snailmember.infrastructure.event.processor.DiscordNotifier
import com.bockerl.snailmember.infrastructure.outbox.enums.EventType
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberCreateEvent
import com.bockerl.snailmember.utils.FORMATTED_ID
import com.bockerl.snailmember.utils.createMember
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import org.springframework.kafka.core.KafkaTemplate
import java.util.concurrent.CompletableFuture

class MemberLogEventPublisherTests :
    BehaviorSpec({
        val kafkaTemplate = mockk<KafkaTemplate<String, Any>>()
        val discordNotifier = mockk<DiscordNotifier>()
        lateinit var publisher: MemberLogEventPublisher

        beforeContainer {
            clearAllMocks(answers = false)
            publisher =
                MemberLogEventPublisher(
                    kafkaTemplate = kafkaTemplate,
                    discordNotifier = discordNotifier,
                )
        }

        Given("회원 관련 요청이") {
            val newMember = createMember()
            val event =
                MemberCreateEvent(
                    memberId = FORMATTED_ID,
                    memberEmail = newMember.memberEmail,
                    memberLanguage = newMember.memberLanguage,
                    memberStatus = newMember.memberStatus,
                    memberPhoto = newMember.memberPhoto,
                    memberBirth = newMember.memberBirth,
                    memberNickname = newMember.memberNickname,
                    memberRegion = newMember.memberRegion,
                    memberPhoneNumber = newMember.memberPhoneNumber,
                    memberGender = newMember.memberGender,
                    signUpPath = newMember.signupPath,
                )
            val topicSlot = slot<String>()
            val keySlot = slot<String>()
            val eventSlot = slot<BaseMemberEvent>()

            When("Service에 도달할다 경우") {
                every { kafkaTemplate.send(any(), any(), any()) } answers {
                    val result = null
                    CompletableFuture.completedFuture(result)
                }
                publisher.memberLogging(event)

                Then("Kafka에 MemberLogEvent가 발행된다") {
                    verify(exactly = 1) {
                        kafkaTemplate.send(capture(topicSlot), capture(keySlot), capture(eventSlot))
                    }
                    val topic = topicSlot.captured
                    val key = keySlot.captured
                    val sendEvent = eventSlot.captured
                    topic shouldNotBe null
                    topic shouldBe EventType.MEMBER_LOGGING.topic
                    key shouldNotBe null
                    key shouldBe event.memberId
                    sendEvent shouldNotBe null
                    sendEvent.memberId shouldBe event.memberId
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
                publisher.memberLogging(event)

                Then("Discord에 알림이 간다") {
                    val topic = topicSlot.captured
                    verify(exactly = 1) {
                        discordNotifier.notify(topic, "Kafka send failed")
                    }
                }
            }
        }
    })