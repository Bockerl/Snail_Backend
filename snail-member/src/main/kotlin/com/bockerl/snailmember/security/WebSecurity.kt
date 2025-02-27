package com.bockerl.snailmember.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class WebSecurity(
    private val authenticationConfiguration: AuthenticationConfiguration,
    private val redisTemplate: RedisTemplate<String, String>,
    private val userDetailsService: UserDetailsService,
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain = http
        .cors { cors -> cors.configure(http) }
        .csrf { csrf -> csrf.disable() }
        .headers { headers ->
            headers.frameOptions { frame ->
                frame.sameOrigin()
            }
        }
        .formLogin { form ->
            form.loginPage("/login")
            form.defaultSuccessUrl("/home-page", false) // false는 항상 이 URL로 리다이렉트하지 않음을 의미
        }
        .logout { logout ->
            logout.logoutUrl("/api/member/logout") // URL 앞의 '/' 추가
            logout.logoutSuccessUrl("/login")
            // logout.logoutSuccessHandler(customLogoutHandler)  // 핸들러 설정 시
        }
        .authorizeHttpRequests { auth ->
            auth.requestMatchers("/swagger-ui.html/**").permitAll()
                .requestMatchers("/login").permitAll()
                .requestMatchers("/api/registration/**").permitAll()
                .requestMatchers("/api/user/oauth2").permitAll()
                .requestMatchers("/api/member/health").permitAll()
                .anyRequest().authenticated()
        }
        .sessionManagement { session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        }
//        .addFilterBefore(AuthorizationFilter, UsernamePasswordAuthenticationFilter::class.java) // JWT 인증 필터
//        .addFilterAt(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java) // 커스텀 인증 필터
        .build()

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(
        config: AuthenticationConfiguration,
        userService: UserDetailsService,
        encoder: BCryptPasswordEncoder,
    ): AuthenticationManager = config.authenticationManager.apply {
        val provider = DaoAuthenticationProvider().apply {
            setPasswordEncoder(encoder)
            setUserDetailsService(userDetailsService)
        }
        (this as ProviderManager).providers.add(provider)
    }
}
