package com.bockerl.snailmember.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.security.web.FilterChainProxy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer

@OpenAPIDefinition(
    info = Info(
        title = "Snail API 명세서",
        description = "Snail API 명세서",
        version = "v1",
    ),
)
@Configuration
class OpenApiConfig(private val applicationContext: ApplicationContext) {

    @Bean
    @Profile("!Prod")
    fun areaApi(): GroupedOpenApi = createGroupedOpenApi(
        groupName = "활동지역 관련 api",
        paths = arrayOf("/api/area/**"),
    )

    @Bean
    @Profile("!Prod")
    fun userApi(): GroupedOpenApi = createGroupedOpenApi(
        groupName = "멤버 관련 api",
        paths = arrayOf("/api/member/**"),
    )

    @Bean
    @Profile("!Prod")
    fun registrationApi(): GroupedOpenApi = createGroupedOpenApi(
        groupName = "회원가입 관련 api",
        paths = arrayOf("/api/registration/**"),
    )

    @Bean
    @Profile("!Prod")
    fun boardApi(): GroupedOpenApi = createGroupedOpenApi(
        groupName = "게시글 관련 api",
        paths = arrayOf("/api/board/**"),
    )

    @Bean
    @Profile("!Prod")
    fun gatheringApi(): GroupedOpenApi = createGroupedOpenApi(
        groupName = "모임 관련 api",
        paths = arrayOf("/api/gathering/**"),
    )

    @Bean
    @Profile("!Prod")
    fun fileApi(): GroupedOpenApi = createGroupedOpenApi(
        groupName = "파일 관련 api",
        paths = arrayOf("/api/file/**"),
    )

    // 로그인 API 엔드포인트 추가
    @Bean
    @Profile("!Prod")
    fun loginApi(): GroupedOpenApi = GroupedOpenApi
        .builder()
        .group("로그인 관련 api")
        .pathsToMatch("/api/member/login")
        .addOpenApiCustomizer(loginEndpointCustomizer())
        .addOpenApiCustomizer(buildSecurityOpenApi())
        .build()

    // 로그인 엔드포인트 커스터마이저를 빈으로 등록
    @Bean
    fun loginEndpointCustomizer(): OpenApiCustomizer = springSecurityLoginEndpointCustomizer(applicationContext)

    private fun createGroupedOpenApi(groupName: String, paths: Array<String>): GroupedOpenApi = GroupedOpenApi
        .builder()
        .group(groupName)
        .pathsToMatch(*paths)
        .addOpenApiCustomizer(buildSecurityOpenApi())
        .build()

    private fun buildSecurityOpenApi(): OpenApiCustomizer = OpenApiCustomizer { openApi ->
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

    /**
     * Swagger 문서에서 로그인 엔드포인트를 표시하기 위한 커스터마이저
     * Spring Security의 UsernamePasswordAuthenticationFilter를 감지하여
     * 해당 엔드포인트의 API 문서를 동적으로 생성합니다.
     */
    fun springSecurityLoginEndpointCustomizer(applicationContext: ApplicationContext): OpenApiCustomizer {
        val filterChainProxy = applicationContext.getBean(
            AbstractSecurityWebApplicationInitializer.DEFAULT_FILTER_NAME,
            FilterChainProxy::class.java,
        )

        return OpenApiCustomizer { openAPI ->
            for (filterChain in filterChainProxy.filterChains) {
                val optionalFilter = filterChain.filters
                    .filterIsInstance<UsernamePasswordAuthenticationFilter>()
                    .firstOrNull()

                optionalFilter?.let { usernamePasswordAuthenticationFilter ->
                    val operation = Operation()

                    // 요청 바디 스키마 생성
                    val schema: Schema<Any> = ObjectSchema()
                        .addProperty(
                            "memberEmail",
                            StringSchema()._default("email@email.com")
                                .description("로그인에 사용할 이메일"),
                        )
                        .addProperty(
                            "memberPassword",
                            StringSchema()._default("password")
                                .description("로그인에 사용할 비밀번호"),
                        )

                    // 요청 바디 설정
                    val requestBody = RequestBody().content(
                        Content().addMediaType(
                            org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                            MediaType().schema(schema),
                        ),
                    )
                    operation.requestBody(requestBody)

                    // 응답 정의
                    val apiResponses = ApiResponses()

                    // 성공 응답 (200 OK)
                    apiResponses.addApiResponse(
                        HttpStatus.OK.value().toString(),
                        ApiResponse().description(HttpStatus.OK.reasonPhrase)
                            .content(
                                Content().addMediaType(
                                    org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                    MediaType().example(
                                        @Suppress("ktlint:standard:max-line-length")
                                        "{\"accessToken\":\"sample-access-token\",\"refreshToken\":\"sample-refresh-token\"}",
                                    ),
                                ),
                            ),
                    )
                    // 실패 응답 (401 UNAUTHORIZED)
                    apiResponses.addApiResponse(
                        HttpStatus.UNAUTHORIZED.value().toString(),
                        ApiResponse().description(HttpStatus.UNAUTHORIZED.reasonPhrase)
                            .content(
                                Content().addMediaType(
                                    org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                    MediaType().example("{\"error\":\"UNAUTHORIZED\"}"),
                                ),
                            ),
                    )

                    operation.responses(apiResponses)
                    operation.addTagsItem("member-authentication")
                    operation.summary("로그인")
                    operation.description("이메일로 가입한 회원의 로그인")

                    // 경로 아이템 생성 및 추가
                    val pathItem = PathItem().post(operation)
                    openAPI.paths.addPathItem("/api/member/login", pathItem)
                }
            }
        }
    }
}
