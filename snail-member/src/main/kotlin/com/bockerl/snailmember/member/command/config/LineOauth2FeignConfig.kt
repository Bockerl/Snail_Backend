package com.bockerl.snailmember.member.command.config

import feign.codec.EncodeException
import feign.codec.Encoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LineOauth2FeignConfig {
    // 필요한 경우 중첩된 객체 구조 처리
    @Bean
    fun lineFeignEncoder(): Encoder =
        Encoder { obj, _, template ->
            when (obj) {
                is String -> template.body(obj)
                else -> throw EncodeException("Only String type is supported")
            }
        }
}