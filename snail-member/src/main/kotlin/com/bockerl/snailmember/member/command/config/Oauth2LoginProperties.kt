package com.bockerl.snailmember.member.command.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class KaKaoLoginProperties(
    // 코드 요청 시, redirect uri
    @Value("\${KAKAO_REDIRECT_URI}")
    val kakaoRedirectUri: String,
    // rest api key
    @Value("\${KAKAO_CLIENT_ID}")
    val kakaoClientId: String,
    // 인증 보안을 위한 secret key
    @Value("\${KAKAO_CLIENT_SECRET}")
    val kakaoClientSecret: String,
)
