package com.bockerl.snailchat.infrastructure.config

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
    fun chatApi(): GroupedOpenApi =
        createGroupedOpenApi(
            groupName = "채팅 관련 api",
            paths = arrayOf("/api/chat/**"),
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