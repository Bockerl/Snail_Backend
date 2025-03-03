package com.bockerl.snailmember.security.config

import com.bockerl.snailmember.common.ResponseDTO
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler

class CustomLogoutSuccessfulHandler : LogoutSuccessHandler {
    override fun onLogoutSuccess(
        request: HttpServletRequest?,
        response: HttpServletResponse,
        authentication: Authentication?,
    ) {
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        val responseDTO = ResponseDTO.ok("로그아웃 성공")

        // JSON 문자열로 변환
        val json = ObjectMapper().writeValueAsString(responseDTO)
        response.writer.write(json)
    }
}