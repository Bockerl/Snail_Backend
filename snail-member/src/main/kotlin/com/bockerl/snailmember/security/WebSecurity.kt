package com.bockerl.snailmember.security

import com.bockerl.snailmember.member.command.application.service.CommandMemberService
import com.bockerl.snailmember.member.query.service.QueryMemberService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class WebSecurity(
    private val redisTemplate: RedisTemplate<String, String>,
    private val queryMemberService: QueryMemberService,
    private val commandMemberService: CommandMemberService,
    private val authenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val environment: Environment,
    private val jwtUtils: JwtUtils,
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        val authenticationManagerBuilder =
            http.getSharedObject(AuthenticationManagerBuilder::class.java) as AuthenticationManagerBuilder
        authenticationManagerBuilder.userDetailsService(queryMemberService)
            .passwordEncoder(passwordEncoder())
        val authenticationManager = authenticationManagerBuilder.build()
        return http
            .cors { cors -> cors.configure(http) }
            .csrf { csrf -> csrf.disable() }
            .headers { headers ->
                headers.frameOptions { frame ->
                    frame.sameOrigin()
                }
            }
            .logout { logout ->
                logout.logoutUrl("/api/member/logout")
                logout.logoutSuccessUrl("/api/member/login")
                // logout.logoutSuccessHandler(customLogoutHandler)  // 핸들러 설정 시
            }
//             예외 발생 시, 처리할 exceptionHandler 추가
            .exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint(authenticationEntryPoint)
            }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/swagger-resources/**").permitAll()
                    .requestMatchers("/v3/api-docs/**").permitAll()
                    .requestMatchers("/api/member/login").permitAll()
                    .requestMatchers("/api/registration/**").permitAll()
                    .requestMatchers("/api/user/oauth2/**").permitAll()
                    .requestMatchers("/favicon.ico").permitAll()
                    .requestMatchers("/api/member/health").permitAll()
                    .anyRequest().authenticated()
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authenticationManager(authenticationManager)
            .addFilterBefore(
                JwtFilter(
                    queryMemberService,
                    commandMemberService,
                    jwtUtils,
                    redisTemplate,
                    environment,
                ),
                UsernamePasswordAuthenticationFilter::class.java,
            ) // JWT 인증 필터
            .addFilterAt(
                getAuthenticationFilter(authenticationManager),
                UsernamePasswordAuthenticationFilter::class.java,
            ) // 커스텀 인증 필터
            .build()
    }

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder(10)

    fun getAuthenticationFilter(authenticationManager: AuthenticationManager): AuthenticationFilter {
        val authenticationFilter = AuthenticationFilter(
            queryMemberService = queryMemberService,
            commandMemberService = commandMemberService,
            authenticationManager = authenticationManager,
            redisTemplate = redisTemplate,
            environment = environment,
        )
        authenticationFilter.setFilterProcessesUrl("/api/member/login")
        return authenticationFilter
    }
}
