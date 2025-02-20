package com.bockerl.snailchat.testConfig

import org.springframework.boot.test.context.TestConfiguration

@TestConfiguration
class TestConfiguration {
    /* TestConfiguration 설정
        1. Application Text가 중복 생성되는 것을 막고, 테스트 환경에서 한 번만 등록되도록 설정한다.
        2. @Primary를 붙여 실제 Bean과 겹치지 않도록 우선순위를 높인다.
     */
}