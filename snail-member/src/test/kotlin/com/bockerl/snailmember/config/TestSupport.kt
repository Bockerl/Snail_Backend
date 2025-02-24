package com.bockerl.snailmember.config

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestExecutionListeners

@SpringBootTest(classes = [TestConfiguration::class])
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@TestExecutionListeners(
    listeners = [
        ContextLoadListener::class,
        ResetMockTestExecutionListener::class,
    ],
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS,
)
abstract class TestSupport
