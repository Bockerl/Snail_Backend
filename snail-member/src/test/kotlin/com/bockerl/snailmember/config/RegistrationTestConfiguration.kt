package com.bockerl.snailmember.config

import com.bockerl.snailmember.member.command.application.service.AuthService
import com.bockerl.snailmember.member.command.domain.repository.TempMemberRepository
import org.mockito.Mockito.mock
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class RegistrationTestConfiguration {
    @Primary
    @Bean
    fun mockAuthService() = mock(AuthService::class.java)!!

    @Primary
    @Bean
    fun mockTempMemberRepository() = mock(TempMemberRepository::class.java)!!
}