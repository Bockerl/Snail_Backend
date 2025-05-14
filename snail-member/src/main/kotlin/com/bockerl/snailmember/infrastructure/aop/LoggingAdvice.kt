package com.bockerl.snailmember.infrastructure.aop

import com.bockerl.snailmember.common.event.DomainFailEvent
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
            eventPublisher.publishEvent(domainFailEvent)
            throw e
        }

    private fun extractDomainName(joinPoint: ProceedingJoinPoint): String {
        val packageName = joinPoint.signature.declaringType.packageName
        return when {
            "member" in packageName -> "MEMBER"
            "area" in packageName -> "AREA"
            "board" in packageName -> "BOARD"
            "boardcomment" in packageName -> "BOARD_COMMENT"
            "boardcommentlike" in packageName -> "BOARD_COMMENT_LIKE"
            "boardlike" in packageName -> "BOARDLIKE"
            "boardrecomment" in packageName -> "BOARDRECOMMENT"
            "boardrecommentlike" in packageName -> "BOARDRECOMMENT_LIKE"
            "infrastructure" in packageName -> "INFRASTRUCTURE"
            "search" in packageName -> "SEARCH"
            else -> "UNKNOWN"
        }
    }
}