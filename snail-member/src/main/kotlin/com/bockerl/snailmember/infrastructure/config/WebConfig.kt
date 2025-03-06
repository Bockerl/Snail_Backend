package com.bockerl.snailmember.infrastructure.config

import com.bockerl.snailmember.security.config.CurrentMemberIdArgumentResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    @Autowired
    private lateinit var currentMemberIdArgumentResolver: CurrentMemberIdArgumentResolver

    override fun addArgumentResolvers(argumentResolvers: MutableList<HandlerMethodArgumentResolver>) {
        argumentResolvers.add(currentMemberIdArgumentResolver)
    }
}