package com.bockerl.snailmember.config

import org.springframework.test.context.TestContext
import org.springframework.test.context.TestExecutionListener

class ContextLoadListener : TestExecutionListener {
    companion object {
        private var contextId: String? = null
        private var loadCount = 0
    }

    override fun beforeTestClass(testContext: TestContext) {
        val currentContextId = testContext.applicationContext.id
        if (contextId != currentContextId) {
            contextId = currentContextId
            loadCount++
            println(
                """
                ApplicationContext 새로 초기화됨
                ContextId: $contextId
                총 초기화 횟수: $loadCount
                테스트 클래스: ${testContext.testClass.simpleName}
                """.trimIndent(),
            )
        }
    }
}
