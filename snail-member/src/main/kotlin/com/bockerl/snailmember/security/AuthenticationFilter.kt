package com.bockerl.snailmember.security

import com.bockerl.snailmember.member.query.service.QueryMemberService
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

class AuthenticationFilter(
    private val memberService: QueryMemberService,
    private val redisTemplate: RedisTemplate<String, String>,
) : UsernamePasswordAuthenticationFilter() {

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        super.doFilter(request, response, chain)
    }
}
