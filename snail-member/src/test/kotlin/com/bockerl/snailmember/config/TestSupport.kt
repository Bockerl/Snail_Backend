package com.bockerl.snailmember.config

import org.springframework.test.context.TestExecutionListeners

@TestExecutionListeners(
    listeners = [
        ContextLoadListener::class,
        ResetMockTestExecutionListener::class,
    ],
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS,
)
abstract class TestSupport