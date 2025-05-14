@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.member.command.application.service

import com.bockerl.snailmember.common.event.DomainFailEvent
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.file.command.application.service.CommandFileService
import com.bockerl.snailmember.infrastructure.aop.LoggingAdvice
import com.bockerl.snailmember.infrastructure.outbox.service.OutboxService
import com.bockerl.snailmember.member.command.domain.repository.MemberRepository
import com.bockerl.snailmember.member.command.domain.service.CommandMemberServiceImpl
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory
import org.springframework.context.ApplicationEventPublisher

class LoggingAspectTests :
    BehaviorSpec({
        val memberRepository = mockk<MemberRepository>()
        val fileService = mockk<CommandFileService>()
        val outboxService = mockk<OutboxService>()
        val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
        val objectMapper = ObjectMapper()

        lateinit var proxy: CommandMemberService

        // 의존성 초기화 완료 후, 테스트 바로 직전에만 실행해서 프록시 초기화
        beforeSpec {
            val impl =
                spyk(
                    CommandMemberServiceImpl(
                        memberRepository = memberRepository,
                        commandFileService = fileService,
                        outboxService = outboxService,
                        eventPublisher = eventPublisher,
                        objectMapper = objectMapper,
                    ),
                    recordPrivateCalls = true,
                )
            // 순수 Java 기반의 AOP 프록시 생성 도구
            val factory = AspectJProxyFactory(impl)
            // 프록시에 aspect 추가
            factory.addAspect(LoggingAdvice(eventPublisher))
            // aop 동작을 위해 수동으로 proxy 생성 후, 캐스팅
            proxy = factory.getProxy() as CommandMemberService
        }

        Given("@Logging annotation이 붙은 서비스 메서드에서") {
            val slot = slot<Any>()
            When("예외가 발생하면") {
                every {
                    memberRepository.findMemberByMemberEmailAndMemberStatusNot(any(), any())
                } answers {
                    throw CommonException(ErrorCode.NOT_FOUND_MEMBER)
                }
                Then("DomainFailEvent가 발생한다") {
                    shouldThrow<CommonException> {
                        proxy.putLastAccessTime(
                            email = "test@test.com",
                            ipAddress = "127.0.0.1",
                            userAgent = "MockAgent",
                            idempotencyKey = "unique-key",
                        )
                    }

                    verify(exactly = 1) {
                        eventPublisher.publishEvent(capture(slot))
                    }

                    with(slot.captured as DomainFailEvent) {
                        domainName shouldBe "MEMBER"
                        methodName shouldBe "putLastAccessTime"
                    }
                }
            }
        }
    })