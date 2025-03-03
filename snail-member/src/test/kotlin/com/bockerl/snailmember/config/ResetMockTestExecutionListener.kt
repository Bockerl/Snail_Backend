package com.bockerl.snailmember.config

import org.mockito.internal.util.MockUtil
import org.springframework.test.context.TestContext
import org.springframework.test.context.TestExecutionListener

class ResetMockTestExecutionListener : TestExecutionListener {
    // TestConfig 클래스를 사용해서 테스트에 사용될 mock을 하나의 context에 관리하다 보니
    // 테스트의 isolation이 힘들어졌습니다. 예시로 redisTemplate을 주로 사용하는
    // 제 테스트 코드는 이전 테스트 코드가 다음 테스트 코드에 영향을 줘서 제대로 된 테스트를 진행할 수 없었습니다.
    // 따라서 TestExecutionListener 인터페이스가 제공하는 메서드 중 하나인 afterTestMethod를 사용해서
    // 매 테스트마다 bean을 초기화하도록 했습니다.
    override fun afterTestMethod(testContext: TestContext) {
        val applicationContext = testContext.applicationContext

        // 모든 bean load
        applicationContext.beanDefinitionNames.forEach { beanName ->
            val bean = applicationContext.getBean(beanName)

            // mock인 bean만 reset
            if (MockUtil.isMock(bean)) MockUtil.resetMock(bean)
        }
    }
}