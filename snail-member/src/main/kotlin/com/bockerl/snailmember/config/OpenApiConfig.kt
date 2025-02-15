/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@OpenAPIDefinition(
    info =
        Info(
            title = "Snail API 명세서",
            description = "Snail API 명세서",
            version = "v1",
        ),
)
@Configuration
class OpenApiConfig {
    @Bean
    @Profile("!Prod")
    fun userApi(): GroupedOpenApi =
        createGroupedOpenApi(
            groupName = "멤버 관련 api",
            paths = arrayOf("/api/member/**"),
        )

    @Bean
    @Profile("!Prod")
    fun registrationApi(): GroupedOpenApi =
        createGroupedOpenApi(
            groupName = "회원가입 관련 api",
            paths = arrayOf("/api/registration/**"),
        )

    @Bean
    @Profile("!Prod")
    fun boardApi(): GroupedOpenApi =
        createGroupedOpenApi(
            groupName = "게시글 관련 api",
            paths = arrayOf("/api/board/**"),
        )

    @Bean
    @Profile("!Prod")
    fun boardLikeApi(): GroupedOpenApi =
        createGroupedOpenApi(
            groupName = "게시글 좋아요 관련 api",
            paths = arrayOf("/api/board-like/**"),
        )

    @Bean
    @Profile("!Prod")
    fun gatheringApi(): GroupedOpenApi =
        createGroupedOpenApi(
            groupName = "모임 관련 api",
            paths = arrayOf("/api/gathering/**"),
        )

    @Bean
    @Profile("!Prod")
    fun fileApi(): GroupedOpenApi =
        createGroupedOpenApi(
            groupName = "파일 관련 api",
            paths = arrayOf("/api/file/**"),
        )

    private fun createGroupedOpenApi(
        groupName: String,
        paths: Array<String>,
    ): GroupedOpenApi =
        GroupedOpenApi
            .builder()
            .group(groupName)
            .pathsToMatch(*paths)
            .addOpenApiCustomizer(buildSecurityOpenApi())
            .build()

    private fun buildSecurityOpenApi(): OpenApiCustomizer =
        OpenApiCustomizer { openApi ->
            openApi.addSecurityItem(SecurityRequirement().addList("jwt token"))
            openApi.components.addSecuritySchemes(
                "jwt token",
                SecurityScheme().apply {
                    name = "Authorization"
                    type = SecurityScheme.Type.HTTP
                    `in` = SecurityScheme.In.HEADER
                    bearerFormat = "JWT"
                    scheme = "bearer"
                },
            )
        }
}