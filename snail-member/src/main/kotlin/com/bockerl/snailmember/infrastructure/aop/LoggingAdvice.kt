package com.bockerl.snailmember.infrastructure.aop

import com.bockerl.snailmember.common.event.DomainFailEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Aspect
@Component
class LoggingAdvice(
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val logger = KotlinLogging.logger { }

    @Around("@annotation(com.bockerl.snailmember.infrastructure.aop.Logging)")
    fun atTarget(joinPoint: ProceedingJoinPoint): Any? =
        try {
            joinPoint.proceed()
        } catch (e: Exception) {
            val domainFailEvent =
                DomainFailEvent(
                    domainName = extractDomainName(joinPoint),
                    methodName = joinPoint.signature.name,
                    message = e.message ?: e.javaClass.name,
                    cause = (e.cause ?: "UNKNOWN").toString(),
                )
            logger.info { "domainFailEvent: $domainFailEvent" }
            eventPublisher.publishEvent(domainFailEvent)
            throw e
        }

    private fun extractDomainName(joinPoint: ProceedingJoinPoint): String {
        val packageName = joinPoint.signature.declaringType.packageName
        println("packageName: $packageName")
        val subPackge = packageName.substringAfter("com.bockerl.snailmember.")
        println("subPackge: $subPackge")
        return when {
            "member" in subPackge -> "MEMBER"
            "area" in subPackge -> "AREA"
            "board" in subPackge -> "BOARD"
            "boardcomment" in subPackge -> "BOARD_COMMENT"
            "boardcommentlike" in subPackge -> "BOARD_COMMENT_LIKE"
            "boardlike" in subPackge -> "BOARDLIKE"
            "boardrecomment" in subPackge -> "BOARDRECOMMENT"
            "boardrecommentlike" in subPackge -> "BOARDRECOMMENT_LIKE"
            "infrastructure" in subPackge -> "INFRASTRUCTURE"
            "search" in subPackge -> "SEARCH"
            else -> "UNKNOWN"
        }
    }
}