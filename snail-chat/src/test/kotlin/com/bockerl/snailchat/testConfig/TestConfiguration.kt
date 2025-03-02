package com.bockerl.snailchat.testConfig

import com.bockerl.snailchat.chat.command.application.mapper.VoToDtoConverter
import com.bockerl.snailchat.chat.command.application.service.CommandChatMessageService
import com.bockerl.snailchat.chat.command.domain.repository.CommandChatMessageRepository
import org.mockito.Mockito.mock
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class TestConfiguration {
    /* TestConfiguration 설정
        1. Application Text가 중복 생성되는 것을 막고, 테스트 환경에서 한 번만 등록되도록 설정한다.
        2. @Primary를 붙여 실제 Bean과 겹치지 않도록 우선순위를 높인다.
     */

    // commandChatService 등록
    @Primary
    @Bean
    fun mockCommandChatMessageService() = mock(CommandChatMessageService::class.java)!!

    // VoToDtoConverter 등록
    @Primary
    @Bean
    fun mockVoToDtoConverter() = mock(VoToDtoConverter::class.java)!!

    // DB 저장을 위해서 Repository 등록
    @Primary
    @Bean
    fun mockCommandChatMessageRepository() = mock(CommandChatMessageRepository::class.java)!!
}