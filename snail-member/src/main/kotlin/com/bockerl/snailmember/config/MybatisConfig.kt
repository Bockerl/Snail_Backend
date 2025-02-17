/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.config

import com.bockerl.snailmember.area.query.config.JsonTypeHandler
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.ibatis.annotations.Mapper
import org.mybatis.spring.annotation.MapperScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@MapperScan(basePackages = ["com.bockerl.snailmember.*.query.repository"], annotationClass = Mapper::class)
class MybatisConfig {
    @Bean
    fun objectMapper(): ObjectMapper =
        ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) // 알 수 없는 필드 무시
            // 날짜를 배열이 아니라 ISO-8601 문자열 등으로 출력하도록 설정
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }

    @Bean
    fun jsonTypeHandler(objectMapper: ObjectMapper): JsonTypeHandler = JsonTypeHandler(objectMapper)
}