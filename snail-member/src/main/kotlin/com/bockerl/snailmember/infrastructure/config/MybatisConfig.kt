/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.infrastructure.config

import com.bockerl.snailmember.area.query.config.JsonTypeHandler
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.type.TypeHandler
import org.mybatis.spring.SqlSessionFactoryBean
import org.mybatis.spring.annotation.MapperScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
@MapperScan(basePackages = ["com.bockerl.snailmember.*.query.repository"], annotationClass = Mapper::class)
class MybatisConfig {
    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
        registerModule(JavaTimeModule())
        enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // 타임스탬프 형식 사용
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    @Bean
    fun jsonTypeHandler(objectMapper: ObjectMapper): JsonTypeHandler = JsonTypeHandler(objectMapper)

    // TypeHandler를 bean을 사용하지 않고 직접 인스턴스를 생성하려고 해서, sqlSessionFactory 설정에서 명시적으로 등록
    @Bean
    fun sqlSessionFactory(dataSource: DataSource, jsonTypeHandler: JsonTypeHandler): SqlSessionFactory {
        val sqlSessionFactoryBean = SqlSessionFactoryBean()
        // 데이터 소스 설정
        sqlSessionFactoryBean.setDataSource(dataSource)
        // TypeHandler 등록 - Array<TypeHandler<*>> 타입으로 변환
        sqlSessionFactoryBean.setTypeHandlers(*arrayOf<TypeHandler<*>>(jsonTypeHandler))

        // SqlSessionFactory 반환
        return sqlSessionFactoryBean.getObject()
            ?: throw IllegalStateException("Failed to create SqlSessionFactory")
    }
}