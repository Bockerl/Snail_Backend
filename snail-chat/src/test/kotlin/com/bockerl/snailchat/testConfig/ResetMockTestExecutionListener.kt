package com.bockerl.snailchat.testConfig

import org.mockito.internal.util.MockUtil
import org.springframework.test.context.TestContext
import org.springframework.test.context.TestExecutionListener

class ResetMockTestExecutionListener : TestExecutionListener {
    /* ResetMockTestExecutionListener란?
        이전 테스트 코드가 다음 테스트 코드에 영향을 미치지 않도록
        매 테스트마다 bean을 초기화 (매번 AfterEach를 하지 않아도 된다)
     */
    override fun afterTestMethod(testContext: TestContext) {
        val applicationContext = testContext.applicationContext

        // 모든 bean을 load
        applicationContext.beanDefinitionNames.forEach { beanName ->
            val bean = applicationContext.getBean(beanName)

            // mock인 bean만 reset
            if (MockUtil.isMock(bean)) MockUtil.resetMock(bean)
        }
    }
}