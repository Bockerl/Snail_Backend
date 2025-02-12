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
            enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // 타임스탬프 형식 사용
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

    @Bean
    fun jsonTypeHandler(objectMapper: ObjectMapper): JsonTypeHandler = JsonTypeHandler(objectMapper)
}